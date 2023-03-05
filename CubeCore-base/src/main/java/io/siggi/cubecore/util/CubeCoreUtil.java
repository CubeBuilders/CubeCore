package io.siggi.cubecore.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

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
}
