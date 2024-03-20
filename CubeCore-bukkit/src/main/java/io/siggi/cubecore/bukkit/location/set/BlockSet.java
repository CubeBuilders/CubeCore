package io.siggi.cubecore.bukkit.location.set;

import io.siggi.cubecore.bukkit.location.BlockLocation;

public interface BlockSet extends Iterable<BlockLocation> {
    boolean contains(BlockLocation location);
}
