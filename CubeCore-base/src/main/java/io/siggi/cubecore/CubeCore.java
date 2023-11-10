package io.siggi.cubecore;

import com.google.gson.GsonBuilder;
import io.siggi.cubecore.apiserver.ApiServer;
import io.siggi.cubecore.apiserver.ApiServerImpl;
import io.siggi.cubecore.apiserver.ApiServerStartException;
import io.siggi.cubecore.usercache.TextureCache;
import io.siggi.cubecore.usercache.UserCache;
import io.siggi.cubecore.userinfo.UserDatabase;
import io.siggi.cubecore.userinfo.UserInfo;
import io.siggi.cubecore.util.text.FormattedText;
import io.siggi.cubecore.util.text.TextPiece;
import io.siggi.nbt.NBTCompound;
import io.siggi.nbt.NBTList;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class CubeCore {
    private static CubeCore instance;
    private final CubeCorePlugin plugin;
    private final File dataFolder;
    private final File userCacheDir;
    private UserDatabase userDatabase;
    private boolean started = false;
    private ApiServer apiServer;

    public CubeCore(CubeCorePlugin plugin, File dataFolder) {
        if (instance != null) {
            throw new IllegalStateException("CubeCore has already been instantiated");
        }
        if (plugin == null || dataFolder == null)
            throw new NullPointerException();
        instance = this;
        this.plugin = plugin;
        this.dataFolder = dataFolder;

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        userCacheDir = new File(dataFolder, "usercache");
        userDatabase = new UserCache(userCacheDir);

        File apiServerConfig = new File(dataFolder, "apiserver.json");
        if (apiServerConfig.exists()) {
            try {
                apiServer = new ApiServerImpl(apiServerConfig, new File(dataFolder, "websessions"));
            } catch (ApiServerStartException e) {
                getLogger().log(Level.SEVERE, e, () -> "Unable to initialize ApiServer");
            }
        }
    }

    public void pluginEnabled() {
        started = true;
        if (apiServer != null) {
            try {
                apiServer.start();
            } catch (ApiServerStartException e) {
                getLogger().log(Level.SEVERE, e, () -> "Unable to start ApiServer");
            }
        }
    }

    public void pluginDisabled() {
        if (apiServer != null) {
            apiServer.close();
        }
    }

    public static CubeCore getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return instance.plugin.getLogger();
    }

    public static ApiServer getApiServer() {
        return instance.apiServer;
    }

    public static void setApiServer(ApiServer apiServer) {
        if (instance.started) {
            throw new IllegalStateException("Cannot set ApiServer after plugin has been enabled.");
        }
        instance.apiServer = apiServer;
    }

    public static UserDatabase getUserDatabase() {
        return instance.userDatabase;
    }

    public static void setUserDatabase(UserDatabase db) {
        if (db == null) throw new NullPointerException();
        instance.userDatabase = db;
    }

    @Deprecated
    public static UserCache getUserCache() {
        UserDatabase db = instance.userDatabase;
        if (db instanceof UserCache) {
            return (UserCache) db;
        }
        throw new IllegalStateException("UserCache is not available on servers using a custom UserDatabase.");
    }

    public static NBTCompound createBook(String title, String author, List<? extends BaseComponent> pages) {
        NBTCompound book = new NBTCompound();
        book.setByte("Count", (byte) 1);
        book.setShort("Damage", (short) 0);
        book.setString("id", "minecraft:written_book");

        NBTCompound tag = new NBTCompound();
        book.setCompound("tag", tag);
        tag.setString("title", title);
        tag.setString("author", author);
        tag.setByte("resolved", (byte) 1);
        tag.setInt("generation", 0);

        NBTList pagesList = new NBTList();
        tag.setList("pages", pagesList);

        for (BaseComponent component : pages) {
            pagesList.addString(ComponentSerializer.toString(component));
        }

        return book;
    }

    @Deprecated
    public static NBTCompound createPlayerHead(UUID player) {
        if (player == null)
            return createPlayerHead(null, null, null, null);
        String name = getUserCache().getName(player);
        TextureCache.Entry entry = getUserCache().getTextures().get(player);
        String payload, signature;
        if (entry == null) {
            payload = signature = null;
        } else {
            payload = entry.getPayload();
            signature = entry.getSignature();
        }
        return createPlayerHead(player, name, payload, signature);
    }

    public static NBTCompound createPlayerHead(UserInfo userInfo) {
        if (userInfo == null) {
            return createPlayerHead(null, null, null, null);
        }
        return createPlayerHead(userInfo.getUUID(), userInfo.getUsername(), userInfo.getTexturesPayload(), userInfo.getTexturesSignature());
    }

    public static NBTCompound createPlayerHead(UUID player, String name, String textures, String texturesSignature) {
        NBTCompound head = new NBTCompound();
        head.setString("id", "minecraft:skull");
        head.setByte("Count", (byte) 1);
        head.setShort("Damage", (short) 3);

        NBTCompound tag = new NBTCompound();

        NBTCompound skullOwner = new NBTCompound();

        if (player != null)
            skullOwner.setString("Id", player.toString());
        if (name != null)
            skullOwner.setString("Name", name);

        if (textures != null && texturesSignature != null) {
            NBTCompound properties = new NBTCompound();
            skullOwner.setCompound("Properties", properties);

            NBTList textureList = new NBTList();
            properties.setList("textures", textureList);

            NBTCompound texturePayload = new NBTCompound();
            textureList.addCompound(texturePayload);
            texturePayload.setString("Value", textures);
            texturePayload.setString("Signature", texturesSignature);
        }

        if (skullOwner.size() > 0) {
            tag.setCompound("SkullOwner", skullOwner);
            head.setCompound("tag", tag);
        }

        return head;
    }

    public static GsonBuilder registerTypeAdapters(GsonBuilder builder) {
        instance.plugin.registerTypeAdapters(builder);
        builder.registerTypeAdapter(FormattedText.class, FormattedText.typeAdapter);
        builder.registerTypeAdapter(TextPiece.class, TextPiece.typeAdapter);
        builder.registerTypeAdapter(ChatColor.class, TextPiece.chatColorTypeAdapter);
        return builder;
    }
}
