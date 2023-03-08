package io.siggi.cubecore.util.text.book;

import io.siggi.cubecore.io.LineReader;
import io.siggi.nbt.NBTCompound;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@FunctionalInterface
public interface BookParser {

    default NBTCompound loadBook(File file, boolean useFallbackColor, boolean forBedrock) throws IOException {
        try (LineReader reader = new LineReader(new FileInputStream(file))) {
            return parseBook(reader, useFallbackColor, forBedrock);
        }
    }

    NBTCompound parseBook(LineReader reader, boolean useFallbackColor, boolean forBedrock) throws IOException;
}
