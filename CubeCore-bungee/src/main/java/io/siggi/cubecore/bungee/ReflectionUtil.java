package io.siggi.cubecore.bungee;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;

public class ReflectionUtil {

    public static class ReflectionUtilListener implements Listener {
        @EventHandler
        public void playerDisconnect(PlayerDisconnectEvent event) {
            playerDisconnected(event.getPlayer());
        }
    }

    private static final Map<ProxiedPlayer,UpstreamBridge> upstreamBridgeMap = new ConcurrentHashMap<>();

    private static void playerDisconnected(ProxiedPlayer player) {
        upstreamBridgeMap.remove(player);
    }

    public static UpstreamBridge getUpstreamBridge(ProxiedPlayer player) {
        UpstreamBridge upstreamBridge = upstreamBridgeMap.get(player);
        if (upstreamBridge == null) {
            upstreamBridgeMap.put(player, upstreamBridge = getUpstreamBridge0(player));
        }
        return upstreamBridge;
    }
    private static UpstreamBridge getUpstreamBridge0(ProxiedPlayer player) {
        try {
            Field chField = UserConnection.class.getDeclaredField("ch");
            chField.setAccessible(true);
            HandlerBoss handlerBoss = ((ChannelWrapper) chField.get(player)).getHandle().pipeline().get(HandlerBoss.class);
            Field handlerField = HandlerBoss.class.getDeclaredField("handler");
            handlerField.setAccessible(true);
            return (UpstreamBridge) handlerField.get(handlerBoss);
        } catch (Exception e) {
            return null;
        }
    }
}
