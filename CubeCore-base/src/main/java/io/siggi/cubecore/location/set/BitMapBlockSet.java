package io.siggi.cubecore.location.set;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.siggi.cubecore.location.BlockLocation;
import io.siggi.cubecore.util.CubeCoreUtil;
import io.siggi.cubecore.util.SimpleIterator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class BitMapBlockSet implements BlockSet {
    public static final TypeAdapter<BitMapBlockSet> typeAdapter = new TypeAdapter<BitMapBlockSet>() {
        @Override
        public BitMapBlockSet read(JsonReader reader) throws IOException {
            byte[] map;
            int minX, minY, minZ;
            int xSize, ySize, zSize;
            String base64String = reader.nextString();
            byte[] data = Base64.getDecoder().decode(base64String);
            ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
            int compressionType = dataStream.read();
            InputStream in = null;
            try {
                switch (compressionType) {
                    case 0:
                        in = dataStream;
                        break;
                    case 1:
                        in = new GZIPInputStream(dataStream);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown compression type " + compressionType);
                }
                DataInputStream dataIn = new DataInputStream(in);
                minX = dataIn.readInt();
                minY = dataIn.readInt();
                minZ = dataIn.readInt();
                xSize = dataIn.readInt();
                ySize = dataIn.readInt();
                zSize = dataIn.readInt();
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    CubeCoreUtil.copy(in, out);
                    map = out.toByteArray();
                }
            } finally {
                try {
                    if (in != null) in.close();
                } catch (IOException ignored) {
                }
            }
            BitMapBlockSet set = new BitMapBlockSet(map, minX, minY, minZ, xSize, ySize, zSize);
            set.compressed = data;
            return set;
        }

        @Override
        public void write(JsonWriter writer, BitMapBlockSet set) throws IOException {
            if (set.compressed == null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream(24 + set.map.length);
                out.write(0);
                DataOutputStream dataOut = new DataOutputStream(out);
                dataOut.writeInt(set.minX);
                dataOut.writeInt(set.minY);
                dataOut.writeInt(set.minZ);
                dataOut.writeInt(set.xSize);
                dataOut.writeInt(set.ySize);
                dataOut.writeInt(set.zSize);
                out.write(set.map);
                byte[] uncompressedData = out.toByteArray();
                out.reset();
                out.write(1);
                try (GZIPOutputStream gzipOut = new GZIPOutputStream(out)) {
                    gzipOut.write(uncompressedData, 1, uncompressedData.length - 1);
                }
                byte[] compressedData = out.toByteArray();
                set.compressed = compressedData.length < uncompressedData.length ? compressedData : uncompressedData;
            }
            writer.value(Base64.getEncoder().encodeToString(set.compressed).replaceAll("[\\r\\n]", ""));
        }
    };

    private final byte[] map;
    private final int minX, minY, minZ;
    private final int xSize, ySize, zSize;
    private final transient CuboidBlockSet region;
    private transient byte[] compressed;

    BitMapBlockSet(byte[] map, int minX, int minY, int minZ, int xSize, int ySize, int zSize) {
        if (map == null) throw new NullPointerException();
        this.map = map;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.region = new CuboidBlockSet(
                new BlockLocation(minX, minY, minZ),
                new BlockLocation(minX + xSize - 1, minY + ySize - 1, minZ + zSize - 1)
        );
    }

    @Override
    public boolean contains(BlockLocation location) {
        int x = location.getX() - minX;
        int y = location.getY() - minY;
        int z = location.getZ() - minZ;
        if (x < 0 || x > xSize || y < 0 || y > ySize || z < 0 || z > zSize) return false;
        int position = x + (z * xSize) + (y * (xSize * zSize));
        int arrayPosition = position / 8;
        int bitPosition = position % 8;
        return ((map[arrayPosition] >>> bitPosition) & 1) != 0;
    }

    @Override
    public Iterator<BlockLocation> iterator() {
        Iterator<BlockLocation> blockIterator = region.iterator();
        return new SimpleIterator<BlockLocation>() {
            @Override
            public BlockLocation getNextValue() throws NoSuchElementException {
                BlockLocation next;
                do {
                    next = blockIterator.next();
                } while (!contains(next));
                return next;
            }
        };
    }

    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof BitMapBlockSet)) return false;
        BitMapBlockSet o = (BitMapBlockSet) other;
        return minX == o.minX && minY == o.minY && minZ == o.minZ
                && xSize == o.xSize && ySize == o.ySize && zSize == o.zSize
                && Arrays.equals(map, o.map);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(minX, minY, minZ, xSize, ySize, zSize);
        result = 31 * result + Arrays.hashCode(map);
        return result;
    }
}
