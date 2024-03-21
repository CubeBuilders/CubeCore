package io.siggi.cubecore.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.siggi.cubecore.io.LineReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import static io.siggi.cubecore.util.OS.CURRENT_OS;

public class CubeCoreUtil {
    private CubeCoreUtil() {
    }

    public static UUID uuidFromString(String string) {
        return UUID.fromString(
            string
                .replace("-", "")
                .replaceAll(
                    "([0-9A-Fa-f]{8})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{12})",
                    "$1-$2-$3-$4-$5"
                )
        );
    }

    public static <T> Iterable<T> iterable(Iterator<T> iterator) {
        return () -> iterator;
    }

    public static <T> Iterator<T> iterateCollectionOfCollection(Collection<? extends Collection<T>> collectionOfCollection) {
        return new SimpleIterator<T>() {
            final Iterator<? extends Collection<T>> iterator = collectionOfCollection.iterator();
            Iterator<T> current = null;

            @Override
            public T getNextValue() throws NoSuchElementException {
                while (current == null || !current.hasNext()) {
                    if (!iterator.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    current = iterator.next().iterator();
                }
                return current.next();
            }

            @Override
            public void remove() {
                current.remove();
            }
        };
    }

    public static TextComponent glueComponents(List<? extends BaseComponent> components) {
        TextComponent holder = new TextComponent("");
        components.forEach(holder::addExtra);
        return holder;
    }

    private static final char[] hexCharset = "0123456789abcdef".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] output = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            output[i * 2] = hexCharset[(bytes[i] >> 4) & 0xf];
            output[i * 2 + 1] = hexCharset[bytes[i] & 0xf];
        }
        return new String(output);
    }

    public static byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string");
        }
        byte[] bytes = new byte[hex.length() / 2];
        try {
            for (int i = 0; i < bytes.length; i++) {
                int offset = i * 2;
                int end = offset + 2;
                bytes[i] = (byte) Integer.parseInt(hex.substring(offset, end), 16);
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Invalid hex string");
        }
        return bytes;
    }

    public static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Load a map from an InputStream. It auto-detects json or per-line key=value properties.
     *
     * @param in the InputStream to read from
     * @return the map.
     * @throws IOException if something goes wrong
     */
    public static Map<String, String> loadMap(InputStream in) throws IOException {
        byte[] data = readFully(in);
        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(data))) {
            JsonObject object = new JsonParser().parse(reader).getAsJsonObject();
            Map<String, String> map = new HashMap<>();
            for (String key : object.keySet()) {
                String value = object.get(key).getAsString();
                map.put(key, value);
            }
            return map;
        } catch (Exception ignored) {
        }
        Map<String, String> map = new HashMap<>();
        try (LineReader lineReader = new LineReader(new ByteArrayInputStream(data))) {
            String line;
            while ((line = lineReader.readLine()) != null) {
                int equalPos = line.indexOf("=");
                if (equalPos == -1) continue;
                String key = line.substring(0, equalPos);
                String value = line.substring(equalPos + 1);
                map.put(key, value);
            }
        }
        return map;
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[4096];
        int c;
        while ((c = in.read(b,0,b.length)) != -1) {
            out.write(b,0,c);
        }
    }

    public static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }

    public static void clone(File from, File to) throws IOException {
        Process process;
        switch (CURRENT_OS) {
            case MACOS: {
                process = Runtime.getRuntime().exec(new String[]{
                        "cp",
                        "-c",
                        "-r",
                        from.getAbsolutePath(),
                        to.getAbsolutePath()
                });
            }
            break;
            case LINUX: {
                process = Runtime.getRuntime().exec(new String[]{
                        "cp",
                        "--reflink=auto",
                        "-r",
                        from.getAbsolutePath(),
                        to.getAbsolutePath()
                });
            }
            break;
            default: {
                copy(from, to);
                return;
            }
        }
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Clone failed, nonzero exit code " + exitCode);
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    private static void copy(File from, File to) throws IOException {
        if (from.isFile()) {
            copyFile(from, to);
        } else if (from.isDirectory()) {
            to.mkdirs();
            for (File file : from.listFiles()) {
                copy(file, new File(to, file.getName()));
            }
        }
    }

    private static void copyFile(File from, File to) throws IOException {
        try (FileInputStream in = new FileInputStream(from);
             FileOutputStream out = new FileOutputStream(to)) {
            copy(in, out);
        }
    }
}
