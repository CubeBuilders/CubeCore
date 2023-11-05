package io.siggi.cubecore.bungee;

import io.siggi.cubecore.session.PlayerSession;
import io.siggi.cubecore.userinfo.UserDatabase;
import java.util.NoSuchElementException;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.Property;

public class EventListenerBungee implements Listener {
    private final UserDatabase cache;

    public EventListenerBungee(UserDatabase cache) {
        this.cache = cache;
    }

    @EventHandler
    public void playerJoin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        LoginResult profile = ((InitialHandler) player.getPendingConnection()).getLoginProfile();
        try {
            Property property = getProperty(profile.getProperties(), "textures");
            String value = property.getValue();
            String signature = property.getSignature();
            cache.storeToCache(player.getUniqueId(), player.getName(), value, signature);
        } catch (NoSuchElementException | NullPointerException e) {
            cache.storeToCache(player.getUniqueId(), player.getName(), null, null);
        }
    }

    @EventHandler
    public void playerQuit(PlayerDisconnectEvent event) {
        PlayerSession.clear(event.getPlayer().getUniqueId());
    }

    private Property getProperty(Property[] properties, String name) {
        for (Property property : properties) {
            if (name.equals(property.getName()))
                return property;
        }
        throw new NoSuchElementException();
    }
}
