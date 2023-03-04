package io.siggi.cubecore.bungee;

import com.google.gson.GsonBuilder;
import io.siggi.cubecore.CubeCore;
import io.siggi.cubecore.CubeCorePlugin;
import io.siggi.cubecore.pluginmessage.OutboundPluginMessageBuilder;
import io.siggi.nbt.NBTCompound;
import io.siggi.nbt.NBTTool;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class CubeCoreBungee extends Plugin implements CubeCorePlugin {
    private static CubeCoreBungee instance;
    private CubeCore cubeCore;

    public static CubeCoreBungee getInstance() {
        return instance;
    }

    /**
     * Send a chat message or command as a player. On 1.19 and up, this may fail for chat messages and commands used for
     * sending chat messages depending on whether chat signatures are enforced or not.
     *
     * @param p
     * @param message
     */
    public static void chatAsPlayer(ProxiedPlayer p, String message) {
        PacketSpoofer.chatAsPlayer(p, message);
    }

    /**
     * Open a book for a player. Only works if the backend server the player is currently connected to has CubeCore.
     *
     * @param p
     * @param pages
     */
    public static void openBook(ProxiedPlayer p, List<? extends BaseComponent> pages) {
        NBTCompound nbtBook = CubeCore.createBook("CubeCore", "CubeCore", pages);
        byte[] serializedBook = NBTTool.serialize(nbtBook);
        CubeCoreMessengerBungee.send(p, new OutboundPluginMessageBuilder("cubecore:openBook").write((out) -> {
            out.writeInt(serializedBook.length);
            out.write(serializedBook);
        }));
    }

    /**
     * Resend Commands packet. Only works if the backend server the player is currently connected to has CubeCore.
     *
     * @param p
     */
    public static void refreshCommands(ProxiedPlayer p) {
        CubeCoreMessengerBungee.send(p, new OutboundPluginMessageBuilder("cubecore:refreshCommands"));
    }

    @Override
    public void onEnable() {
        instance = this;
        this.cubeCore = new CubeCore(this, getDataFolder());
        CacheUpdaterBungee cacheUpdater = new CacheUpdaterBungee(CubeCore.getUserCache());
        getProxy().getPluginManager().registerListener(this, CubeCoreMessengerBungee.getListener());
        getProxy().getPluginManager().registerListener(this, cacheUpdater);

        CubeCoreMessengerBungee.setHandler("cubecore:chatAsPlayer", (p, subChannel, in) -> {
            chatAsPlayer(p, in.readUTF());
        });
    }

    @Override
    public void registerTypeAdapters(GsonBuilder builder) {
    }
}
