package io.siggi.cubecore.location;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Objects;

public final class BlockLocation {
    public static final TypeAdapter<BlockLocation> typeAdapter = new TypeAdapter<BlockLocation>() {
        @Override
        public BlockLocation read(JsonReader reader) throws IOException {
            int x = 0;
            int y = 0;
            int z = 0;
            reader.beginObject();
            while (reader.peek() != JsonToken.END_OBJECT) {
                String name = reader.nextName();
                switch (name) {
                    case "x": {
                        x = reader.nextInt();
                    }
                    break;
                    case "y": {
                        y = reader.nextInt();
                    }
                    break;
                    case "z": {
                        z = reader.nextInt();
                    }
                    break;
                }
            }
            reader.endObject();
            return new BlockLocation(x, y, z);
        }

        @Override
        public void write(JsonWriter writer, BlockLocation location) throws IOException {
            writer.beginObject();
            writer.name("x").value(location.x);
            writer.name("y").value(location.y);
            writer.name("z").value(location.z);
            writer.endObject();
        }
    };

    private final int x, y, z;

    public BlockLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof BlockLocation)) return false;
        BlockLocation o = (BlockLocation) other;
        return x == o.x
                && y == o.y
                && z == o.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
