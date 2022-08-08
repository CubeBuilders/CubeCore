package io.siggi.cubecore.bukkit;

import com.google.gson.GsonBuilder;
import io.siggi.cubecore.CubeCore;
import io.siggi.cubecore.CubeCorePlugin;
import io.siggi.cubecore.bukkit.actionitem.ActionItems;
import io.siggi.cubecore.bukkit.commands.CommandUnsignBook;
import io.siggi.cubecore.bukkit.item.CanonicalItems;
import io.siggi.cubecore.bukkit.location.CubeCoreLocation;
import io.siggi.cubecore.bukkit.location.WorldProviders;
import io.siggi.cubecore.nms.NMSUtil;
import io.siggi.cubecore.pluginmessage.OutboundPluginMessageBuilder;
import io.siggi.nbt.NBTCompound;
import io.siggi.nbt.NBTTool;
import io.siggi.nbt.NBTToolBukkit;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;

public class CubeCoreBukkit extends JavaPlugin implements CubeCorePlugin {
    private static CubeCoreBukkit instance;
    private CubeCore cubeCore;

    public static CubeCoreBukkit getInstance() {
        return instance;
    }

    /**
     * Determine if we are behind a BungeeCord proxy or not. CubeCore detects this by reading the settings.bungeecord
     * setting in the spigot.yml file.
     *
     * @return true if we are behind a BungeeCord proxy.
     */
    public static boolean isBungeeCordServer() {
        return SpigotConfig.bungee;
    }

    /**
     * Send a chat message or command as a player. If the server is a backend server behind a Bungee proxy, chat will be
     * sent to Bungee (which might bounce back as a chat packet if Bungee does not setCancelled(true) on the chat event),
     * otherwise it's passed directly to {@link Player#chat(String)}. On 1.19 and up, this may fail for chat messages
     * and commands used for sending chat messages depending on whether chat signatures are enforced or not.
     *
     * @param p
     * @param message
     */
    public static void chatAsPlayer(Player p, String message) {
        if (isBungeeCordServer()) {
            CubeCoreMessengerBukkit.send(p, new OutboundPluginMessageBuilder("cubecore:chatAsPlayer").write((out) -> {
                out.writeUTF(message);
            }));
        } else {
            p.chat(message);
        }
    }

    public static void openSign(Player p, Block block) {
        NMSUtil.get().openSign(p, block);
    }

    public static void openSign(Player p, Sign sign) {
        NMSUtil.get().openSign(p, sign);
    }

    public static void openBook(Player p, List<? extends BaseComponent> pages) {
        p.openBook(createBook("CubeCore", "CubeCore", pages));
    }

    public static ItemStack createBook(String title, String author, List<? extends BaseComponent> pages) {
        return NBTToolBukkit.itemFromNBT(CubeCore.createBook(title, author, pages));
    }

    public static void setClientSideOpLevel(Player p, int opLevel) {
        NMSUtil.get().setClientSideOpLevel(p, opLevel);
    }

    @Override
    public void onEnable() {
        instance = this;
        this.cubeCore = new CubeCore(this, getDataFolder());
        if (isBungeeCordServer()) {
            getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", CubeCoreMessengerBukkit.getListener());
            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        }

        CommandUnsignBook commandUnsignBook = new CommandUnsignBook();
        getCommand("unsignbook").setExecutor(commandUnsignBook);
        getCommand("unsignbook").setTabCompleter(commandUnsignBook);

        CacheUpdaterBukkit cacheUpdater = new CacheUpdaterBukkit(CubeCore.getUserCache());
        getServer().getPluginManager().registerEvents(cacheUpdater, this);
        for (Listener listener : CanonicalItems.getListeners())
            getServer().getPluginManager().registerEvents(listener, this);
        getServer().getPluginManager().registerEvents(WorldProviders.getListener(), this);
        getServer().getPluginManager().registerEvents(ActionItems.getListener(), this);

        CubeCoreMessengerBukkit.setHandler("cubecore:openBook", (p, subChannel, in) -> {
            byte[] serializedBook = new byte[in.readInt()];
            in.readFully(serializedBook);
            NBTCompound bookNbt = NBTTool.deserialize(serializedBook);
            ItemStack book = NBTToolBukkit.itemFromNBT(bookNbt);
            p.openBook(book);
        });

        CubeCoreMessengerBukkit.setHandler("cubecore:refreshCommands", (p, subChannel, in) -> {
            p.updateCommands();
        });
    }

    @Override
    public void registerTypeAdapters(GsonBuilder builder) {
        builder.registerTypeAdapter(CubeCoreLocation.class, CubeCoreLocation.typeAdapter);
    }
}
