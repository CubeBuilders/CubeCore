package io.siggi.cubecore.location.set;

import io.siggi.cubecore.location.BlockLocation;

import java.util.HashSet;
import java.util.Set;

public class BitMapBlockSetBuilder {
    private final Set<BlockLocation> blocks = new HashSet<BlockLocation>();

    public BitMapBlockSetBuilder add(BlockLocation block) {
        blocks.add(block);
        return this;
    }

    public BitMapBlockSetBuilder addAll(BlockSet set) {
        set.iterator().forEachRemaining(blocks::add);
        return this;
    }

    public BitMapBlockSetBuilder remove(BlockLocation block) {
        blocks.remove(block);
        return this;
    }

    public BitMapBlockSetBuilder removeAll(BlockSet set) {
        set.iterator().forEachRemaining(blocks::remove);
        return this;
    }

    public BitMapBlockSetBuilder retain(BlockSet set) {
        blocks.removeIf(block -> !set.contains(block));
        return this;
    }

    public BitMapBlockSet build() {
        if (blocks.isEmpty()) {
            return new BitMapBlockSet(new byte[0], 0, 0, 0, 0, 0, 0);
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockLocation block : blocks) {
            minX = Math.min(block.getX(), minX);
            minY = Math.min(block.getY(), minY);
            minZ = Math.min(block.getZ(), minZ);
            maxX = Math.max(block.getX(), maxX);
            maxY = Math.max(block.getY(), maxY);
            maxZ = Math.max(block.getZ(), maxZ);
        }
        int xSize = maxX - minX + 1;
        int ySize = maxY - minY + 1;
        int zSize = maxZ - minZ + 1;
        long bitCount = ((long) xSize) * ((long) ySize) * ((long) zSize);
        long arraySize = bitCount / 8;
        if (bitCount % 8 != 0) arraySize += 1;
        if (arraySize > (Integer.MAX_VALUE - 16)) {
            throw new IllegalArgumentException("This BlockSet would require more than 2 GB of memory.");
        }
        byte[] map = new byte[(int) arraySize];
        for (BlockLocation block : blocks) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            int position = x + (z * xSize) + (y * (xSize * zSize));
            int arrayPosition = position / 8;
            int bitPosition = position % 8;
            map[arrayPosition] = (byte) (map[arrayPosition] | (1 << bitPosition));
        }
        return new BitMapBlockSet(map, minX, minY, minZ, xSize, ySize, zSize);
    }
}
