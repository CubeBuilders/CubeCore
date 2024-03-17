package io.siggi.cubecore.bukkit.location;

public interface BlockSet extends Iterable<BlockLocation> {
    boolean contains(BlockLocation location);
}
