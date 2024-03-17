package io.siggi.cubecore.bukkit.location;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.siggi.cubecore.util.CubeCoreUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public final class WorldID {
    public static final TypeAdapter<WorldID> typeAdapter = new TypeAdapter<WorldID>() {
        @Override
        public WorldID read(JsonReader reader) throws IOException {
            String worldName = null;
            UUID worldUid = null;
            reader.beginObject();
            while (reader.peek() != JsonToken.END_OBJECT) {
                String name = reader.nextName();
                switch (name) {
                    case "name": {
                        worldName = reader.nextString();
                    }
                    break;
                    case "uid": {
                        worldUid = CubeCoreUtil.uuidFromString(reader.nextString());
                    }
                    break;
                }
            }
            reader.endObject();
            return new WorldID(worldName, worldUid);
        }

        @Override
        public void write(JsonWriter writer, WorldID worldId) throws IOException {
            writer.beginObject();
            writer.name("name").value(worldId.name);
            writer.name("uid").value(worldId.uid == null ? null : worldId.uid.toString());
            writer.endObject();
        }
    };

    private final String name;
    private final UUID uid;

    public WorldID(String name, UUID uid) {
        if (name == null && uid == null) throw new NullPointerException("name and uid cannot be both null.");
        this.name = name;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public UUID getUid() {
        return uid;
    }

    World getWorld() {
        if (name != null) {
            World world = Bukkit.getWorld(name);
            if (world != null) return world;
        }

        if (uid != null) {
            World world = Bukkit.getWorld(uid);
            if (world != null) return world;
        }

        return WorldProviders.loadWorld(this);
    }

    /**
     * Determine if the world for this location can be loaded.
     *
     * @return true if the world can be loaded, false otherwise.
     */
    public boolean isWorldLoadable() {
        return WorldProviders.isWorldLoadable(this);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof WorldID)) return false;
        WorldID o = (WorldID) other;
        return Objects.equals(name, o.name) && Objects.equals(uid, o.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uid);
    }
}
