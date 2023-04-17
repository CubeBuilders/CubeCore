package io.siggi.cubecore.bukkit.location;

import io.siggi.cubecore.bukkit.CubeCoreBukkit;
import java.io.Closeable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.plugin.Plugin;

public final class GrabbedChunks implements Closeable {
    private static final boolean useTickets;

    static {
        boolean uT = false;
        try {
            Chunk.class.getDeclaredMethod("addPluginChunkTicket", Plugin.class);
            uT = true;
        } catch (NoSuchMethodException ignored) {
        }
        useTickets = uT;
    }

    private static final Map<Chunk, Set<GrabbedChunks>> currentlyGrabbed = new HashMap<>();
    private final Set<Chunk> chunks = new HashSet<>();
    private final Set<Chunk> immutableChunks = Collections.unmodifiableSet(chunks);
    private boolean closed = false;

    GrabbedChunks() {
    }

    public Set<Chunk> getChunks() {
        return immutableChunks;
    }

    void add(Chunk chunk) {
        if (closed) return;
        chunks.add(chunk);
        Set<GrabbedChunks> grabbedChunks = currentlyGrabbed.computeIfAbsent(chunk, c -> new HashSet<>());
        if (grabbedChunks.add(this) && useTickets) {
            chunk.addPluginChunkTicket(CubeCoreBukkit.getInstance());
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        for (Chunk chunk : chunks) {
            Set<GrabbedChunks> grabbed = currentlyGrabbed.get(chunk);
            grabbed.remove(this);
            if (grabbed.isEmpty()) {
                currentlyGrabbed.remove(chunk);
                if (useTickets)
                    chunk.removePluginChunkTicket(CubeCoreBukkit.getInstance());
            }
        }
    }
}
