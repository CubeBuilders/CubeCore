package io.siggi.cubecore.bukkit.location;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Chunk;
import org.bukkit.World;

public final class BukkitChunkGrabber {
    private static BukkitChunkGrabber.ChunkGrabberAbstract implementation = new BukkitChunkGrabber.ChunkGrabberDefaultImplementation();

    public static void setImplementation(BukkitChunkGrabber.ChunkGrabberAbstract impl) {
        if (impl == null) throw new NullPointerException();
        implementation = impl;
    }

    public static CompletableFuture<Chunk> grab(World world, int x, int z, boolean urgently) {
        if (world == null) throw new NullPointerException();
        return implementation.grab(world, x, z, urgently);
    }

    public static CompletableFuture<BukkitGrabbedChunks> grab(Collection<BukkitChunk> chunks, boolean urgently) {
        BukkitGrabbedChunks grabbedChunks = new BukkitGrabbedChunks();
        CompletableFuture<BukkitGrabbedChunks> result = new CompletableFuture<>();
        CompletableFuture<?>[] futuresArray = new CompletableFuture[chunks.size()];
        int i = 0;
        for (BukkitChunk coreChunk : chunks) {
            futuresArray[i++] = grab(coreChunk.getWorld(), coreChunk.getX(), coreChunk.getZ(), urgently).thenAccept(grabbedChunks::add);
        }
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futuresArray);
        allFutures.thenRun(() -> {
            result.complete(grabbedChunks);
        }).exceptionally(throwable -> {
            grabbedChunks.close();
            result.completeExceptionally(throwable);
            return null;
        });
        return result;
    }

    @FunctionalInterface
    public interface ChunkGrabberAbstract {
        CompletableFuture<Chunk> grab(World world, int x, int z, boolean urgently);
    }

    private static class ChunkGrabberDefaultImplementation implements BukkitChunkGrabber.ChunkGrabberAbstract {
        @Override
        public CompletableFuture<Chunk> grab(World world, int x, int z, boolean urgently) {
            try {
                return CompletableFuture.completedFuture(world.getChunkAt(x, z));
            } catch (Exception e) {
                CompletableFuture<Chunk> result = new CompletableFuture<>();
                result.completeExceptionally(e);
                return result;
            }
        }
    }
}
