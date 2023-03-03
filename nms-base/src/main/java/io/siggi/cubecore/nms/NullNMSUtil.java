package io.siggi.cubecore.nms;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import org.bukkit.World;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class NullNMSUtil extends NMSUtil {

    private final SkinSettings allskin = new SkinSettings(127);

    @Nonnull
    @Override
    public SkinSettings getSkinSettings(@Nonnull Player p) {
        return allskin;
    }

    @Nonnull
    @Override
    public ChatSetting getChatSetting(@Nonnull Player p) {
        return ChatSetting.ON;
    }

    @Nullable
    @Override
    public GameProfile getGameProfile(@Nonnull Player p) {
        return null;
    }

    @Override
    public void setRenderDistance(@Nonnull World world, int distance) {
    }

    @Override
    public void sendPacket(@Nonnull Player p, @Nullable Object packet) {
    }

    @Nullable
    @Override
    public Object createPacketSetSkullProfile(@Nonnull Skull skull, @Nullable GameProfile gameProfile) {
        return null;
    }

    @Override
    public boolean isHostile(@Nonnull LivingEntity entity) {
        return false;
    }

    @Override
    public Object createEntityStatusPacket(@Nonnull Entity entity, int status) {
        return null;
    }

    @Override
    public void setWalkDestination(@Nonnull LivingEntity entity, double x, double y, double z) {
    }

    public BrigadierUtil getBrigadierUtil() {
        throw new UnsupportedOperationException();
    }
}
