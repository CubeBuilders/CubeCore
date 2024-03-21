package io.siggi.cubecore.bukkit.location;

import io.siggi.cubecore.location.BlockLocation;
import io.siggi.cubecore.location.CubeCoreLocation;
import io.siggi.cubecore.location.ExactLocation;
import io.siggi.cubecore.location.WorldID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class BukkitLocationUtil {
    private BukkitLocationUtil() {
    }

    public static Block toBukkitBlock(BlockLocation location, World world) {
        return world.getBlockAt(location.getX(), location.getY(), location.getZ());
    }

    public static BlockLocation toBlockLocation(Block block) {
        return new BlockLocation(block.getX(), block.getY(), block.getZ());
    }

    public static Location toBukkitLocation(ExactLocation location, World world) {
        return new Location(
                world,
                location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch()
        );
    }

    public static ExactLocation toExactLocation(Location location) {
        return new ExactLocation(
                location.getX(), location.getY(), location.getZ(),
                location.getPitch(), location.getYaw()
        );
    }

    public static Location toBukkitLocation(CubeCoreLocation location) {
        World world = BukkitWorldProviders.loadWorld(location.getWorld());
        if (world == null)
            return null;
        return new Location(
                world,
                location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch()
        );
    }

    public static CubeCoreLocation toCubeCoreLocation(Location location) {
        World world = location.getWorld();
        if (world == null) throw new NullPointerException("world in location is null");
        WorldID worldID = new WorldID(world.getName(), world.getUID());
        return new CubeCoreLocation(worldID, toExactLocation(location));
    }
}
