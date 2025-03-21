package io.siggi.cubecore.nms.v1_21_R2;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.siggi.cubecore.nms.AuthLibProperty;
import io.siggi.cubecore.nms.ChatSetting;
import io.siggi.cubecore.nms.SkinSettings;
import java.lang.reflect.InvocationTargetException;
import javax.annotation.Nonnull;

import net.minecraft.core.IRegistryCustom;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityStatus;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.EnumChatVisibility;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.TileEntitySkull;
import org.bukkit.Bukkit;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_21_R2.CraftServer;
import org.bukkit.craftbukkit.v1_21_R2.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_21_R2.block.CraftSkull;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftPlayer;
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
        EntityPlayer handle = pl.getHandle();
        return new SkinSettings((Integer) handle.au().a(PA.getClientSettings()));
    }

    @Nonnull
    @Override
    public ChatSetting getChatSetting(@Nonnull Player p) {
        CraftPlayer pl = (CraftPlayer) p;
        EntityPlayer handle = pl.getHandle();
        EnumChatVisibility nmsVisibility = handle.E();
        if (nmsVisibility == null) {
            return ChatSetting.ON;
        }
        switch (nmsVisibility) {
            case a:
                return ChatSetting.ON;
            case b:
                return ChatSetting.COMMANDS_ONLY;
            case c:
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
            org.bukkit.craftbukkit.v1_21_R2.CraftWorld cw = (org.bukkit.craftbukkit.v1_21_R2.CraftWorld) world;
            WorldServer handle = cw.getHandle();
            getMethod(PlayerChunkMap.class, "a", int.class).invoke(handle.m().a, distance);
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
        cp.getHandle().f.b((Packet<?>) packet);
    }

    @Override
    public Object createPacketSetSkullProfile(@Nonnull Skull skull, GameProfile gameProfile) {
        CraftSkull cs = (CraftSkull) skull;
        TileEntitySkull tileEntity;
        try {
            // getTileEntity() changed from public to protected, so we have to use reflection now.
            tileEntity = (TileEntitySkull) getMethod(CraftBlockEntityState.class, "getTileEntity").invoke(cs);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
        NBTTagCompound tag = tileEntity.a(registryAccess());
        if (gameProfile == null) {
            // r = remove
            tag.r("profile");
        } else {
            NBTBase gameProfileTag = ResolvableProfile.a.encodeStart(DynamicOpsNBT.a, new ResolvableProfile(gameProfile)).getOrThrow();
            // a = set
            tag.a("profile", gameProfileTag);
        }
        // TileEntityTypes.o = skull
        return PacketPlayOutTileEntityData.a(tileEntity, (te, ra) -> tag);
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

        return new PacketPlayOutEntityStatus(nmsEntity, (byte) status);
    }

    @Override
    public void setWalkDestination(@Nonnull LivingEntity entity, double x, double y, double z) {
        CraftLivingEntity ce = (CraftLivingEntity) entity;
        EntityLiving ent = ce.getHandle();
        if (ent instanceof EntityInsentient) {
            EntityInsentient ei = (EntityInsentient) ent;
            ei.L().a(x, y, z, 1.0D);
        }
    }

    private MinecraftServer minecraftServer;

    private MinecraftServer getMinecraftServer() {
        if (minecraftServer == null) {
            minecraftServer = ((CraftServer) Bukkit.getServer()).getHandle().b();
        }
        return minecraftServer;
    }

    private IRegistryCustom.Dimension registryAccess() {
        return getMinecraftServer().ba();
    }

    private abstract static class PA extends EntityHuman {

        private PA() {
            super(null, null, 0.0f, null);
        }

        public static DataWatcherObject<?> getClientSettings() {
            return bX;
        }

        public static DataWatcherObject<?> getMainHandSetting() {
            return bY;
        }
    }
}
