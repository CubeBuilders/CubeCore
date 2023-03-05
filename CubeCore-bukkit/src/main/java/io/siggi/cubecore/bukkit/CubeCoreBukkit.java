package io.siggi.cubecore.bukkit;

import com.google.gson.GsonBuilder;
import io.siggi.cubecore.CubeCore;
import io.siggi.cubecore.CubeCorePlugin;
import io.siggi.cubecore.bukkit.actionitem.ActionItems;
import io.siggi.cubecore.bukkit.commands.CommandUnsignBook;
import io.siggi.cubecore.bukkit.item.CanonicalItems;
import io.siggi.cubecore.bukkit.location.CubeCoreLocation;
import io.siggi.cubecore.bukkit.location.WorldProviders;
import io.siggi.cubecore.pluginmessage.OutboundPluginMessageBuilder;
import io.siggi.cubecore.util.DataAuthentication;
import io.siggi.nbt.NBTCompound;
import io.siggi.nbt.NBTTool;
import io.siggi.nbt.NBTToolBukkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
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

    public static void openBook(Player p, List<? extends BaseComponent> pages) {
        p.openBook(createBook("CubeCore", "CubeCore", pages));
    }

    public static ItemStack createBook(String title, String author, List<? extends BaseComponent> pages) {
        return NBTToolBukkit.itemFromNBT(CubeCore.createBook(title, author, pages));
    }

    @Override
    public void onEnable() {
        instance = this;
        this.cubeCore = new CubeCore(this, getDataFolder());
        if (!new File("CubeCore/usercache/names.txt").exists())
        try (BufferedReader reader = new BufferedReader(new FileReader("plugins/PlugCubeBuildersIn/UUIDs.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    int pos = line.indexOf("=");
                    if (pos == -1) continue;
                    UUID uuid = UUID.fromString(line.substring(0, pos));
                    String name = line.substring(pos + 1);
                    CubeCore.getUserCache().getNames().store(uuid, name);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        DataAuthentication.setupSalt(new File(getDataFolder(), "salt.txt"));
        if (isBungeeCordServer()) {
            getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", CubeCoreMessengerBukkit.getListener());
            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        }

        CommandUnsignBook commandUnsignBook = new CommandUnsignBook();
        getCommand("unsignbook").setExecutor(commandUnsignBook);
        getCommand("unsignbook").setTabCompleter(commandUnsignBook);

        EventListenerBukkit eventListener = new EventListenerBukkit(CubeCore.getUserCache());
        getServer().getPluginManager().registerEvents(eventListener, this);
        for (Listener listener : CanonicalItems.getListeners())
            getServer().getPluginManager().registerEvents(listener, this);
        getServer().getPluginManager().registerEvents(WorldProviders.getListener(), this);
        getServer().getPluginManager().registerEvents(ActionItems.getListener(), this);

        BrandReceiverBukkit brandReceiverBukkit = new BrandReceiverBukkit();
        try {
            getServer().getMessenger().registerIncomingPluginChannel(this, "MC|Brand", brandReceiverBukkit);
        } catch (IllegalArgumentException e) {
            getServer().getMessenger().registerIncomingPluginChannel(this, "minecraft:brand", brandReceiverBukkit);
        }

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

    /**
     * Do not call this method. Use {@link CubeCore#registerTypeAdapters(GsonBuilder)} instead.
     */
    @Override
    public void registerTypeAdapters(GsonBuilder builder) {
        builder.registerTypeAdapter(CubeCoreLocation.class, CubeCoreLocation.typeAdapter);
    }
}
