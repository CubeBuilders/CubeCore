package io.siggi.cubecore.bukkit.location;

import java.util.UUID;
import org.bukkit.World;

public interface WorldProvider {
    public World loadWorld(String name);

    public World loadWorld(UUID uuid);
}
