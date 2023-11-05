package io.siggi.cubecore.bukkit;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.siggi.cubecore.nms.AuthLibProperty;
import io.siggi.cubecore.nms.NMSUtil;
import io.siggi.cubecore.session.PlayerSession;
import io.siggi.cubecore.userinfo.UserDatabase;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListenerBukkit implements Listener {
    private final UserDatabase cache;

    public EventListenerBukkit(UserDatabase cache) {
        this.cache = cache;
    }

    @EventHandler
    public void playerJoin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        GameProfile profile = NMSUtil.get().getGameProfile(player);
        try {
            Property property = profile.getProperties().get("textures").iterator().next();
            AuthLibProperty wProperty = NMSUtil.get().wrapProperty(property);
            String value = wProperty.value();
            String signature = wProperty.signature();
            cache.storeToCache(player.getUniqueId(), player.getName(), value, signature);
        } catch (NoSuchElementException | NullPointerException e) {
            cache.storeToCache(player.getUniqueId(), player.getName(), null, null);
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        PlayerSession.clear(event.getPlayer().getUniqueId());
    }
}
