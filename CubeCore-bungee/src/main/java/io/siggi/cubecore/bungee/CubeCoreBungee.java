package io.siggi.cubecore.bungee;

import com.google.gson.GsonBuilder;
import io.siggi.cubecore.CubeCore;
import io.siggi.cubecore.CubeCorePlugin;
import io.siggi.cubecore.bedrockapi.BedrockDeviceInfo;
import io.siggi.cubecore.pluginmessage.OutboundPluginMessageBuilder;
import io.siggi.cubecore.util.DataAuthentication;
import io.siggi.nbt.NBTCompound;
import io.siggi.nbt.NBTTool;
import java.io.File;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.protocol.ProtocolConstants;

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
        DataAuthentication.setupSalt(new File(getDataFolder(), "salt.txt"));
        EventListenerBungee eventListener = new EventListenerBungee(CubeCore.getUserCache());
        getProxy().getPluginManager().registerListener(this, CubeCoreMessengerBungee.getListener());
        getProxy().getPluginManager().registerListener(this, eventListener);

        BrandReceiverBungee brandReceiver = new BrandReceiverBungee();
        getProxy().getPluginManager().registerListener(this, brandReceiver);

        CubeCoreMessengerBungee.setHandler("cubecore:chatAsPlayer", (p, subChannel, in) -> {
            chatAsPlayer(p, in.readUTF());
        });
    }

    /**
     * Do not call this method. Use {@link CubeCore#registerTypeAdapters(GsonBuilder)} instead.
     */
    @Override
    public void registerTypeAdapters(GsonBuilder builder) {
    }

    public static boolean shouldUseFallbackColors(PendingConnection pendingConnection) {
        UUID uuid = pendingConnection.getUniqueId();
        return (uuid != null && BedrockDeviceInfo.isOnBedrock(uuid))
            || pendingConnection.getVersion() < ProtocolConstants.MINECRAFT_1_16;
    }

    public static boolean shouldUseFallbackColors(ProxiedPlayer player) {
        return shouldUseFallbackColors(player.getPendingConnection());
    }
}
