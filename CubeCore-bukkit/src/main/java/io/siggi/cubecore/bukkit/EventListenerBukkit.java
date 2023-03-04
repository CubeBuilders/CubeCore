package io.siggi.cubecore.bukkit;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.siggi.cubecore.nms.NMSUtil;
import io.siggi.cubecore.session.PlayerSession;
import io.siggi.cubecore.usercache.UserCache;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListenerBukkit implements Listener {
    private final UserCache cache;

    public EventListenerBukkit(UserCache cache) {
        this.cache = cache;
    }

    @EventHandler
    public void playerJoin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        cache.getNames().store(player.getUniqueId(), player.getName());

        GameProfile profile = NMSUtil.get().getGameProfile(player);
        try {
            Property property = profile.getProperties().get("textures").iterator().next();
            String value = property.getValue();
            String signature = property.getSignature();
            cache.getTextures().store(player.getUniqueId(), value, signature);
        } catch (NoSuchElementException | NullPointerException e) {
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        PlayerSession.clear(event.getPlayer().getUniqueId());
    }
}
