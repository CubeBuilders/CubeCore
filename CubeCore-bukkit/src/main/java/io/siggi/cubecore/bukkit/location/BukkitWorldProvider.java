package io.siggi.cubecore.bukkit.location;

import io.siggi.cubecore.location.WorldID;
import org.bukkit.World;

public interface BukkitWorldProvider {

    boolean isWorldLoadable(WorldID worldId);

    World loadWorld(WorldID worldId);
}
