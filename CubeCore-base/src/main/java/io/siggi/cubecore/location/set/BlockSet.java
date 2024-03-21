package io.siggi.cubecore.location.set;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.siggi.cubecore.location.BlockLocation;

import java.io.IOException;

public interface BlockSet extends Iterable<BlockLocation> {
    TypeAdapter<BlockSet> typeAdapter = new TypeAdapter<BlockSet>() {

        @Override
        public BlockSet read(JsonReader reader) throws IOException {
            switch (reader.peek()) {
                case NULL:
                    reader.nextNull();
                    return null;
                case STRING:
                    return BitMapBlockSet.typeAdapter.read(reader);
                case BEGIN_OBJECT:
                    return CuboidBlockSet.typeAdapter.read(reader);
                default:
                    throw new IllegalArgumentException("Could not determine BlockSet type from JsonReader.peek()");
            }
        }

        @Override
        public void write(JsonWriter writer, BlockSet set) throws IOException {
            if (set == null) {
                writer.nullValue();
            } else if (set instanceof BitMapBlockSet) {
                BitMapBlockSet.typeAdapter.write(writer, (BitMapBlockSet) set);
            } else if (set instanceof CuboidBlockSet) {
                CuboidBlockSet.typeAdapter.write(writer, (CuboidBlockSet) set);
            } else {
                throw new IllegalArgumentException("Unsupported BlockSet type " + set.getClass().getName());
            }
        }
    };

    boolean contains(BlockLocation location);
}
