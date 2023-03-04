package io.siggi.cubecore.bukkit.location;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.siggi.cubecore.util.CubeCoreUtil;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public final class CubeCoreLocation {
    public static final TypeAdapter<CubeCoreLocation> typeAdapter = new TypeAdapter<CubeCoreLocation>() {
        @Override
        public CubeCoreLocation read(JsonReader reader) throws IOException {
            String worldName = null;
            UUID worldUid = null;
            double x = 0.0;
            double y = 0.0;
            double z = 0.0;
            float pitch = 0.0f;
            float yaw = 0.0f;
            reader.beginObject();
            while (reader.peek() != JsonToken.END_OBJECT) {
                String name = reader.nextName();
                switch (name) {
                    case "worldName": {
                        worldName = reader.nextString();
                    }
                    break;
                    case "worldUid": {
                        worldUid = CubeCoreUtil.uuidFromString(reader.nextString());
                    }
                    break;
                    case "x": {
                        x = reader.nextDouble();
                    }
                    break;
                    case "y": {
                        y = reader.nextDouble();
                    }
                    break;
                    case "z": {
                        z = reader.nextDouble();
                    }
                    break;
                    case "pitch": {
                        pitch = (float) reader.nextDouble();
                    }
                    break;
                    case "yaw": {
                        yaw = (float) reader.nextDouble();
                    }
                    break;
                }
            }
            reader.endObject();
            return new CubeCoreLocation(worldName, worldUid, x, y, z, pitch, yaw);
        }

        @Override
        public void write(JsonWriter writer, CubeCoreLocation location) throws IOException {
            writer.beginObject();
            writer.name("worldName").value(location.worldName);
            writer.name("worldUid").value(location.worldUid.toString());
            writer.name("x").value(location.x);
            writer.name("y").value(location.y);
            writer.name("z").value(location.z);
            if (location.pitch != 0.0f || location.yaw != 0.0f) {
                writer.name("pitch").value(location.pitch);
                writer.name("yaw").value(location.yaw);
            }
            writer.endObject();
        }
    };
    private final String worldName;
    private final UUID worldUid;
    private final double x;
    private final double y;
    private final double z;
    private final float pitch;
    private final float yaw;

    public CubeCoreLocation(Entity entity) {
        this(entity.getLocation());
    }

    public CubeCoreLocation(Location location) {
        this(
            location.getWorld().getName(), location.getWorld().getUID(),
            location.getX(), location.getY(), location.getZ(),
            location.getPitch(), location.getYaw()
        );
    }

    public CubeCoreLocation(String worldName, UUID worldUid, double x, double y, double z, float pitch, float yaw) {
        if (worldName == null && worldUid == null) {
            throw new NullPointerException("worldName and worldUid cannot be both null!");
        }
        this.worldName = worldName;
        this.worldUid = worldUid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public String getWorldName() {
        return worldName;
    }

    public UUID getWorldUid() {
        return worldUid;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    private World getWorld() {
        World world;

        if (worldName != null) {
            world = Bukkit.getWorld(worldName);
            if (world != null) return world;
        }

        if (worldUid != null) {
            world = Bukkit.getWorld(worldUid);
            if (world != null) return world;
        }

        if (worldName != null) {
            world = WorldProviders.loadWorld(worldName);
            if (world != null) return world;
        }

        if (worldUid != null) {
            world = WorldProviders.loadWorld(worldUid);
            if (world != null) return world;
        }

        return null;
    }

    /**
     * Convert this CubeCoreLocation to a Bukkit Location, loading the world if needed. Do not call this method if you
     * don't want to load a world that is currently unloaded.
     *
     * @return the Bukkit Location.
     */
    public Location toBukkitLocation() {
        World world = getWorld();
        if (world == null)
            return null;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof CubeCoreLocation))
            return false;
        CubeCoreLocation o = (CubeCoreLocation) other;
        return Objects.equals(worldName, o.worldName)
            && Objects.equals(worldUid, o.worldUid)
            && x == o.x
            && y == o.y
            && z == o.z
            && pitch == o.pitch
            && yaw == o.yaw;
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, worldUid, x, y, z, pitch, yaw);
    }
}
