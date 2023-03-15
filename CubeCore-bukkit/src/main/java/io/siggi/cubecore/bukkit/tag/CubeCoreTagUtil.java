package io.siggi.cubecore.bukkit.tag;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.bukkit.Material;

class CubeCoreTagUtil {
    private CubeCoreTagUtil() {
    }

    static CubeCoreTag<Material> createMaterialTag(Predicate<Material> materialPredicate) {
        CubeCoreTagImpl<Material> tag = new CubeCoreTagImpl<>();
        for (Material material : Material.values()) {
            if (material.name().startsWith("LEGACY_"))
                continue;
            if (materialPredicate.test(material))
                tag.set.add(material);
        }
        return tag;
    }

    static CubeCoreTag<Material> createMaterialTag(String... names) {
        List<String> namesList = Arrays.asList(names);
        return createMaterialTag((material) -> namesList.contains(material.name()));
    }

    private static class CubeCoreTagImpl<T> implements CubeCoreTag<T> {

        private final Set<T> set = new HashSet<>();
        private final Set<T> immutableSet = Collections.unmodifiableSet(set);

        private CubeCoreTagImpl() {
        }

        @Override
        public boolean isTagged(@Nonnull T value) {
            return set.contains(value);
        }

        @Nonnull
        @Override
        public Set<T> getValues() {
            return immutableSet;
        }
    }
}
