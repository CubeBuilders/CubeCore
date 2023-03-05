package io.siggi.cubecore.util;

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
}
