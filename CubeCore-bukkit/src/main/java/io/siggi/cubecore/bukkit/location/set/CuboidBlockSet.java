package io.siggi.cubecore.bukkit.location.set;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.siggi.cubecore.bukkit.location.BlockLocation;
import io.siggi.cubecore.util.SimpleIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class CuboidBlockSet implements BlockSet {
    public static final TypeAdapter<CuboidBlockSet> typeAdapter = new TypeAdapter<CuboidBlockSet>() {
        @Override
        public CuboidBlockSet read(JsonReader reader) throws IOException {
            BlockLocation from = null;
            BlockLocation to = null;
            reader.beginObject();
            while (reader.peek() != JsonToken.END_OBJECT) {
                String name = reader.nextName();
                switch (name) {
                    case "from": {
                        from = BlockLocation.typeAdapter.read(reader);
                    }
                    break;
                    case "to": {
                        to = BlockLocation.typeAdapter.read(reader);
                    }
                    break;
                }
            }
            reader.endObject();
            return new CuboidBlockSet(from, to);
        }

        @Override
        public void write(JsonWriter writer, CuboidBlockSet set) throws IOException {
            writer.beginObject();
            writer.name("from");
            BlockLocation.typeAdapter.write(writer, set.from);
            writer.name("to");
            BlockLocation.typeAdapter.write(writer, set.to);
            writer.endObject();
        }
    };

    private final BlockLocation from;
    private final BlockLocation to;

    public CuboidBlockSet(BlockLocation from, BlockLocation to) {
        if (from == null || to == null) throw new NullPointerException();
        if (from.getX() <= to.getX()
                && from.getY() <= to.getY()
                && from.getZ() <= to.getZ()) {
            this.from = from;
            this.to = to;
        } else {
            this.from = new BlockLocation(
                    Math.min(from.getX(), to.getX()),
                    Math.min(from.getY(), to.getY()),
                    Math.min(from.getZ(), to.getZ())
            );
            this.to = new BlockLocation(
                    Math.max(from.getX(), to.getX()),
                    Math.max(from.getY(), to.getY()),
                    Math.max(from.getZ(), to.getZ())
            );
        }
    }

    @Override
    public boolean contains(BlockLocation location) {
        return location.getX() >= from.getX() && location.getX() <= to.getX()
                && location.getY() >= from.getY() && location.getY() <= to.getY()
                && location.getZ() >= from.getZ() && location.getZ() <= to.getZ();
    }

    @Override
    public Iterator<BlockLocation> iterator() {
        return new SimpleIterator<BlockLocation>() {
            int x = from.getX() - 1;
            int y = from.getY();
            int z = from.getZ();

            @Override
            public BlockLocation getNextValue() throws NoSuchElementException {
                x += 1;
                if (x > to.getX()) {
                    x = from.getX();
                    z += 1;
                    if (z > to.getZ()) {
                        z = from.getZ();
                        y += 1;
                        if (y > to.getY()) {
                            throw new NoSuchElementException();
                        }
                    }
                }
                return new BlockLocation(x, y, z);
            }
        };
    }

    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof CuboidBlockSet)) return false;
        CuboidBlockSet o = (CuboidBlockSet) other;
        return from.equals(o.from) && to.equals(o.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
