package io.siggi.cubecore.bukkit.location;

import java.util.UUID;
import org.bukkit.World;

public interface WorldProvider {

    boolean isWorldLoadable(UUID worldUuid, String name);

    World loadWorld(UUID worldUuid, String name);
}
