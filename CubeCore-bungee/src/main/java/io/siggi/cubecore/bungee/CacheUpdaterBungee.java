package io.siggi.cubecore.bungee;

import io.siggi.cubecore.usercache.UserCache;
import java.util.NoSuchElementException;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.Property;

public class CacheUpdaterBungee implements Listener {
    private final UserCache cache;

    public CacheUpdaterBungee(UserCache cache) {
        this.cache = cache;
    }

    @EventHandler
    public void playerJoin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        cache.getNames().store(player.getUniqueId(), player.getName());

        LoginResult profile = ((InitialHandler) player.getPendingConnection()).getLoginProfile();
        try {
            Property property = getProperty(profile.getProperties(), "textures");
            String value = property.getValue();
            String signature = property.getSignature();
            cache.getTextures().store(player.getUniqueId(), value, signature);
        } catch (NoSuchElementException | NullPointerException e) {
        }
    }

    private Property getProperty(Property[] properties, String name) {
        for (Property property : properties) {
            if (name.equals(property.getName()))
                return property;
        }
        throw new NoSuchElementException();
    }
}
