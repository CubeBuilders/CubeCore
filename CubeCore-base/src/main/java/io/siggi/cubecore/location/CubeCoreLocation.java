package io.siggi.cubecore.location;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public final class CubeCoreLocation {
    public static final TypeAdapter<CubeCoreLocation> typeAdapter = new TypeAdapter<CubeCoreLocation>() {
        @Override
        public CubeCoreLocation read(JsonReader reader) throws IOException {
            WorldID world = null;
            ExactLocation location = null;
            reader.beginObject();
            while (reader.peek() != JsonToken.END_OBJECT) {
                String name = reader.nextName();
                switch (name) {
                    case "world": {
                        world = WorldID.typeAdapter.read(reader);
                    }
                    break;
                    case "location": {
                        location = ExactLocation.typeAdapter.read(reader);
                    }
                    break;
                }
            }
            reader.endObject();
            return new CubeCoreLocation(world, location);
        }

        @Override
        public void write(JsonWriter writer, CubeCoreLocation location) throws IOException {
            writer.beginObject();
            writer.name("world");
            WorldID.typeAdapter.write(writer, location.world);
            writer.name("location");
            ExactLocation.typeAdapter.write(writer, location.location);
            writer.endObject();
        }
    };

    private final WorldID world;
    private final ExactLocation location;

    public CubeCoreLocation(WorldID world, ExactLocation location) {
        if (world == null) {
            throw new NullPointerException("worldId cannot be null!");
        }
        if (location == null) {
            throw new NullPointerException("location cannot be null!");
        }
        this.world = world;
        this.location = location;
    }

    public WorldID getWorld() {
        return world;
    }

    public String getWorldName() {
        return world.getName();
    }

    public UUID getWorldUid() {
        return world.getUid();
    }

    public ExactLocation getLocation() {
        return location;
    }

    public double getX() {
        return location.getX();
    }

    public double getY() {
        return location.getY();
    }

    public double getZ() {
        return location.getZ();
    }

    public float getPitch() {
        return location.getPitch();
    }

    public float getYaw() {
        return location.getYaw();
    }

    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof CubeCoreLocation))
            return false;
        CubeCoreLocation o = (CubeCoreLocation) other;
        return Objects.equals(world, o.world)
            && Objects.equals(location, o.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, location);
    }
}
