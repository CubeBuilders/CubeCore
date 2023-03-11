package io.siggi.cubecore.bukkit.location;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

public class Teleporter {
    private static TeleporterAbstract implementation = new TeleporterDefaultImplementation();

    public static void setImplementation(TeleporterAbstract impl) {
        if (impl == null) throw new NullPointerException();
        implementation = impl;
    }

    public static CompletableFuture<Boolean> teleport(Entity entity, Location destination) {
        return teleport(entity, destination, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public static CompletableFuture<Boolean> teleport(Entity entity, Location destination, PlayerTeleportEvent.TeleportCause teleportCause) {
        return implementation.teleport(entity, destination, teleportCause);
    }

    @FunctionalInterface
    public interface TeleporterAbstract {
        CompletableFuture<Boolean> teleport(Entity entity, Location destination, PlayerTeleportEvent.TeleportCause teleportCause);
    }

    private static class TeleporterDefaultImplementation implements TeleporterAbstract {
        @Override
        public CompletableFuture<Boolean> teleport(Entity entity, Location destination, PlayerTeleportEvent.TeleportCause teleportCause) {
            try {
                return CompletableFuture.completedFuture(entity.teleport(destination, teleportCause));
            } catch (Exception e) {
                CompletableFuture<Boolean> result = new CompletableFuture<>();
                result.completeExceptionally(e);
                return result;
            }
        }
    }
}
