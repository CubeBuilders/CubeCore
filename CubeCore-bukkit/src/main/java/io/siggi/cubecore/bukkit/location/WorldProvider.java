package io.siggi.cubecore.bukkit.location;

import org.bukkit.World;

public interface WorldProvider {

    boolean isWorldLoadable(WorldID worldId);

    World loadWorld(WorldID worldId);
}
