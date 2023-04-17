package io.siggi.cubecore.bukkit.location;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class CubeCoreChunk {
    private final World world;
    private final int x;
    private final int z;

    public CubeCoreChunk(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public CubeCoreChunk(Block block) {
        this(block.getWorld(), block.getX() >> 4, block.getZ() >> 4);
    }

    public CubeCoreChunk(Location location) {
        this(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public CubeCoreChunk getRelative(int x, int z) {
        if (x == 0 && z == 0) return this;
        return new CubeCoreChunk(world, this.x + x, this.z + z);
    }
}
