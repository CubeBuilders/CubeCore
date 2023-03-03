package io.siggi.cubecore.nms.v1_13_R1;

import com.mojang.authlib.GameProfile;
import io.siggi.cubecore.nms.ChatSetting;
import io.siggi.cubecore.nms.SkinSettings;
import java.lang.reflect.InvocationTargetException;
import javax.annotation.Nonnull;
import net.minecraft.server.v1_13_R1.BlockPosition;
import net.minecraft.server.v1_13_R1.DataWatcherObject;
import net.minecraft.server.v1_13_R1.EntityHuman;
import net.minecraft.server.v1_13_R1.EntityInsentient;
import net.minecraft.server.v1_13_R1.EntityLiving;
import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.GameProfileSerializer;
import net.minecraft.server.v1_13_R1.NBTTagCompound;
import net.minecraft.server.v1_13_R1.Packet;
import net.minecraft.server.v1_13_R1.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_13_R1.PacketPlayOutOpenSignEditor;
import net.minecraft.server.v1_13_R1.PacketPlayOutTileEntityData;
import net.minecraft.server.v1_13_R1.TileEntitySign;
import net.minecraft.server.v1_13_R1.TileEntitySkull;
import net.minecraft.server.v1_13_R1.WorldServer;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_13_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_13_R1.block.CraftSkull;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
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
        return new SkinSettings(((Integer) handle.getDataWatcher().get(PA.getClientSettings())));
    }

    @Nonnull
    @Override
    public ChatSetting getChatSetting(@Nonnull Player p) {
        CraftPlayer pl = (CraftPlayer) p;
        EntityPlayer handle = pl.getHandle();
        EntityHuman.EnumChatVisibility nmsVisibility = handle.getChatFlags();
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
            org.bukkit.craftbukkit.v1_13_R1.CraftWorld cw = (org.bukkit.craftbukkit.v1_13_R1.CraftWorld) world;
            WorldServer handle = cw.getHandle();
            handle.getPlayerChunkMap().a(distance);
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
        cp.getHandle().playerConnection.sendPacket((Packet<?>) packet);
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
        NBTTagCompound tag = tileEntity.aa_();
        int x = tag.getInt("x");
        int y = tag.getInt("y");
        int z = tag.getInt("z");
        BlockPosition bp = new BlockPosition(x, y, z);
        if (gameProfile == null) {
            tag.remove("Owner");
        } else {
            NBTTagCompound gameProfileTag = new NBTTagCompound();
            GameProfileSerializer.serialize(gameProfileTag, gameProfile);
            tag.set("Owner", gameProfileTag);
        }
        return new PacketPlayOutTileEntityData(bp, 4, tag);
    }

    @Override
    public boolean isHostile(@Nonnull LivingEntity entity) {
        if (entity instanceof Monster) {
            return true;
            // below are mobs that are hostile but are not a subclass of Monster
        } else if (entity instanceof Ghast) {
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
    public int getPing(@Nonnull Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }

    @Override
    public void openSign(@Nonnull Player player, @Nonnull Sign sign) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer nmsPlayer = craftPlayer.getHandle();

        TileEntitySign nmsSign;
        try {
            nmsSign = (TileEntitySign) getMethod(sign, "getTileEntity").invoke(sign);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        nmsSign.isEditable = true;
        nmsSign.a((EntityHuman) nmsPlayer);
        nmsPlayer.playerConnection.sendPacket(new PacketPlayOutOpenSignEditor(nmsSign.getPosition()));
    }

    @Override
    public Object createEntityStatusPacket(@Nonnull Entity entity, int status) {
        CraftEntity craftEntity = (CraftEntity) entity;
        net.minecraft.server.v1_13_R1.Entity nmsEntity = craftEntity.getHandle();

        return new PacketPlayOutEntityStatus(nmsEntity, (byte) status);
    }

    @Override
    public void setWalkDestination(@Nonnull LivingEntity entity, double x, double y, double z) {
        CraftLivingEntity ce = (CraftLivingEntity) entity;
        EntityLiving ent = ce.getHandle();
        if (ent instanceof EntityInsentient) {
            EntityInsentient ei = (EntityInsentient) ent;
            ei.getNavigation().a(x, y, z, 1.0D);
        }
    }

    private abstract static class PA extends EntityHuman {

        private PA() {
            super(null, null);
        }

        public static DataWatcherObject<?> getClientSettings() {
            return bx;
        }

        public static DataWatcherObject<?> getMainHandSetting() {
            return by;
        }
    }
}
