package io.siggi.cubecore.bukkit.tag;

import java.util.Set;
import javax.annotation.Nonnull;
import org.bukkit.Material;
import static io.siggi.cubecore.bukkit.tag.CubeCoreTagUtil.createMaterialTag;

public interface CubeCoreTag<T> {

    CubeCoreTag<Material> WOODEN_DOOR = createMaterialTag((material) -> {
        return material != Material.IRON_DOOR && material.name().endsWith("_DOOR");
    });
    CubeCoreTag<Material> DOOR = createMaterialTag((material) -> {
        return material.name().endsWith("_DOOR");
    });
    CubeCoreTag<Material> WOODEN_TRAPDOOR = createMaterialTag((material) -> {
        return material != Material.IRON_TRAPDOOR && (material.name().endsWith("_TRAPDOOR") || material.name().equals("TRAP_DOOR"));
    });
    CubeCoreTag<Material> TRAPDOOR = createMaterialTag((material) -> {
        return material.name().endsWith("_TRAPDOOR") || material.name().equals("TRAP_DOOR");
    });
    CubeCoreTag<Material> BUTTON = createMaterialTag((material) -> {
        return material.name().endsWith("_BUTTON");
    });
    CubeCoreTag<Material> BUTTON_OR_LEVER = createMaterialTag((material) -> {
        return material.name().endsWith("_BUTTON") || material.name().equals("LEVER");
    });
    CubeCoreTag<Material> PRESSURE_PLATE = createMaterialTag((material) -> {
        return material.name().endsWith("_PRESSURE_PLATE")
            || material.name().equals("STONE_PLATE")
            || material.name().equals("WOOD_PLATE")
            || material.name().equals("IRON_PLATE")
            || material.name().equals("GOLD_PLATE");
    });
    CubeCoreTag<Material> SIGN = createMaterialTag((material) -> {
        return material.name().endsWith("_SIGN")
            || material.name().equals("SIGN_POST")
            //|| material.name().equals("WALL_SIGN") // covered by _SIGN above
            || material.name().equals("SIGN");
    });
    CubeCoreTag<Material> WALL_SIGN = createMaterialTag((material) -> {
        return material.name().endsWith("_WALL_SIGN")
            || material.name().equals("WALL_SIGN");
    });
    CubeCoreTag<Material> SIGN_POST = createMaterialTag((material) -> {
        return (material.name().endsWith("_SIGN") && !material.name().endsWith("_WALL_SIGN"))
            || material.name().equals("SIGN_POST");
    });

    boolean isTagged(@Nonnull T var1);

    @Nonnull
    Set<T> getValues();
}
