package io.siggi.cubecore.bukkit;

import io.siggi.cubecore.pluginmessage.InboundPluginMessageHandler;
import io.siggi.cubecore.pluginmessage.InboundPluginMessageRouter;
import io.siggi.cubecore.pluginmessage.OutboundPluginMessageBuilder;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class CubeCoreMessengerBukkit implements PluginMessageListener {

    private static final CubeCoreMessengerBukkit instance = new CubeCoreMessengerBukkit();
    private final InboundPluginMessageRouter<Player> router = new InboundPluginMessageRouter<>();
    private CubeCoreMessengerBukkit() {
    }

    public static CubeCoreMessengerBukkit getListener() {
        return instance;
    }

    public static void send(Player p, OutboundPluginMessageBuilder builder) {
        p.sendPluginMessage(CubeCoreBukkit.getInstance(), "BungeeCord", builder.getBytes());
    }

    public static void setHandler(String subChannel, InboundPluginMessageHandler<Player> handler) {
        instance.router.setHandler(subChannel, handler);
    }

    @Override
    public void onPluginMessageReceived(String tag, Player p, byte[] message) {
        if (!tag.equals("BungeeCord"))
            return;
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String channel = in.readUTF();
            if (!channel.equals("CubeCore"))
                return;
            String subChannel = in.readUTF();
            router.handle(p, subChannel, in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
