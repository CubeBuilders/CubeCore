package io.siggi.cubecore;

import com.google.gson.GsonBuilder;
import io.siggi.cubecore.usercache.UserCache;
import io.siggi.cubecore.util.text.FormattedText;
import io.siggi.cubecore.util.text.TextPiece;
import io.siggi.nbt.NBTCompound;
import io.siggi.nbt.NBTList;
import java.io.File;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class CubeCore {
    private static CubeCore instance;
    private final CubeCorePlugin plugin;
    private final File dataFolder;
    private final File userCacheDir;
    private final UserCache userCache;

    public CubeCore(CubeCorePlugin plugin, File dataFolder) {
        if (plugin == null || dataFolder == null)
            throw new NullPointerException();
        this.plugin = plugin;
        this.dataFolder = dataFolder;

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        if (instance != null) {
            throw new IllegalStateException("CubeCore has already been instantiated");
        }
        userCacheDir = new File(dataFolder, "usercache");
        userCache = new UserCache(userCacheDir);
        instance = this;
    }

    public static CubeCore getInstance() {
        return instance;
    }

    public static UserCache getUserCache() {
        return instance.userCache;
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

    public static GsonBuilder registerTypeAdapters(GsonBuilder builder) {
        instance.plugin.registerTypeAdapters(builder);
        builder.registerTypeAdapter(FormattedText.class, FormattedText.typeAdapter);
        builder.registerTypeAdapter(TextPiece.class, TextPiece.typeAdapter);
        builder.registerTypeAdapter(ChatColor.class, TextPiece.chatColorTypeAdapter);
        return builder;
    }
}
