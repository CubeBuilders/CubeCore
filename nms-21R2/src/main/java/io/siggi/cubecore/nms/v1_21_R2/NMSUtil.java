package io.siggi.cubecore.nms.v1_21_R2;

import com.mojang.authlib.GameProfile;
import io.siggi.cubecore.nms.ChatSetting;
import io.siggi.cubecore.nms.SkinSettings;
import javax.annotation.Nonnull;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftSkull;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;

import static net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION;

public class NMSUtil extends io.siggi.cubecore.nms.NMSUtil {

    private final BrigadierUtil brigadierUtil = new BrigadierUtil();

    @Override
    public BrigadierUtil getBrigadierUtil() {
        return brigadierUtil;
    }

    @Nonnull
    @Override
    public SkinSettings getSkinSettings(@Nonnull Player p) {
        CraftPlayer pl = (CraftPlayer) p;
        ServerPlayer handle = pl.getHandle();
        return new SkinSettings((int) handle.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION));
    }

    @Nonnull
    @Override
    public ChatSetting getChatSetting(@Nonnull Player p) {
        CraftPlayer pl = (CraftPlayer) p;
        ServerPlayer handle = pl.getHandle();
        ChatVisiblity nmsVisibility = handle.getChatVisibility();
        if (nmsVisibility == null) {
            return ChatSetting.ON;
        }
        switch (nmsVisibility) {
            case FULL:
                return ChatSetting.ON;
            case SYSTEM:
                return ChatSetting.COMMANDS_ONLY;
            case HIDDEN:
                return ChatSetting.OFF;
        }
        return ChatSetting.ON;
    }

    @Override
    public GameProfile getGameProfile(@Nonnull Player p) {
        CraftPlayer cp = (CraftPlayer) p;
        return cp.getProfile();
    }

    @Override
    public void setRenderDistance(@Nonnull org.bukkit.World world, int distance) {
        try {
            org.bukkit.craftbukkit.CraftWorld cw = (org.bukkit.craftbukkit.CraftWorld) world;
            cw.getHandle().getChunkSource().chunkMap.setServerViewDistance(distance);
        } catch (Exception e) {
        }
    }

    @Override
    public void sendPacket(@Nonnull Player p, Object packet) {
        if (p == null) {
            throw new NullPointerException();
        }
        if (packet == null) {
            return;
        }
        CraftPlayer cp = (CraftPlayer) p;
        cp.getHandle().connection.send((Packet<?>) packet);
    }

    @Override
    public Object createPacketSetSkullProfile(@Nonnull Skull skull, GameProfile gameProfile) {
        CraftSkull cs = (CraftSkull) skull;
        SkullBlockEntity tileEntity = cs.getTileEntity();
        CompoundTag tag = tileEntity.getUpdateTag(registryAccess());
        if (gameProfile == null) {
            tag.remove("profile");
        } else {
            Tag gameProfileTag = ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, new ResolvableProfile(gameProfile)).getOrThrow();
            tag.put("profile", gameProfileTag);
        }
        return ClientboundBlockEntityDataPacket.create(tileEntity, (te, ra) -> tag);
    }

    @Override
    public boolean isHostile(@Nonnull LivingEntity entity) {
        if (entity instanceof Monster) {
            return true;
            // below are mobs that are hostile but are not a subclass of Monster
        } else if (entity instanceof Ghast) {
            return true;
        } else if (entity instanceof Hoglin) {
            return true;
        } else if (entity instanceof MagmaCube) {
            return true;
        } else if (entity instanceof Phantom) {
            return true;
        } else if (entity instanceof PolarBear) {
            return true;
        } else if (entity instanceof Shulker) {
            return true;
        } else if (entity instanceof Slime) {
            return true;
        } else if (entity instanceof Wolf) {
            Wolf wolf = (Wolf) entity;
            return !wolf.isTamed() && wolf.isAngry();
        } else {
            return false;
        }
    }

    @Override
    public Object createEntityStatusPacket(@Nonnull Entity entity, int status) {
        CraftEntity craftEntity = (CraftEntity) entity;
        net.minecraft.world.entity.Entity nmsEntity = craftEntity.getHandle();

        return new ClientboundEntityEventPacket(nmsEntity, (byte) status);
    }

    @Override
    public void setWalkDestination(@Nonnull LivingEntity entity, double x, double y, double z) {
        CraftLivingEntity ce = (CraftLivingEntity) entity;
        net.minecraft.world.entity.LivingEntity ent = ce.getHandle();
        if (ent instanceof net.minecraft.world.entity.Mob) {
            net.minecraft.world.entity.Mob ei = (net.minecraft.world.entity.Mob) ent;
            ei.getNavigation().moveTo(x, y, z, 1.0D);
        }
    }

    private MinecraftServer minecraftServer;

    private MinecraftServer getMinecraftServer() {
        if (minecraftServer == null) {
            minecraftServer = ((CraftServer) Bukkit.getServer()).getHandle().getServer();
        }
        return minecraftServer;
    }

    private RegistryAccess.Frozen registryAccess() {
        return getMinecraftServer().registryAccess();
    }
}
