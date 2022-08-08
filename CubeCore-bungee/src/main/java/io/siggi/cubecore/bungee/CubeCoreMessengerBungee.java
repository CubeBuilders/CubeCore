package io.siggi.cubecore.bungee;

import io.siggi.cubecore.pluginmessage.InboundPluginMessageHandler;
import io.siggi.cubecore.pluginmessage.InboundPluginMessageRouter;
import io.siggi.cubecore.pluginmessage.OutboundPluginMessageBuilder;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CubeCoreMessengerBungee implements Listener {

    private static final CubeCoreMessengerBungee instance = new CubeCoreMessengerBungee();
    private final InboundPluginMessageRouter<ProxiedPlayer> router = new InboundPluginMessageRouter<>();
    private CubeCoreMessengerBungee() {
    }

    public static CubeCoreMessengerBungee getListener() {
        return instance;
    }

    public static void send(ProxiedPlayer p, OutboundPluginMessageBuilder builder) {
        p.getServer().sendData("BungeeCord", builder.getBytes());
    }

    public static void setHandler(String subChannel, InboundPluginMessageHandler<ProxiedPlayer> handler) {
        instance.router.setHandler(subChannel, handler);
    }

    @EventHandler
    public void pluginMessageEvent(PluginMessageEvent event) {
        Connection sender = event.getSender();
        if (!(sender instanceof Server) || !event.getTag().equals("BungeeCord"))
            return;
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
            String channel = in.readUTF();
            if (!channel.equals("CubeCore"))
                return;
            String subChannel = in.readUTF();
            router.handle((ProxiedPlayer) event.getReceiver(), subChannel, in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
