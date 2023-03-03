package io.siggi.cubecore.nms.v1_17_R1;

import com.mojang.authlib.GameProfile;
import io.siggi.cubecore.nms.ChatSetting;
import io.siggi.cubecore.nms.SkinSettings;
import java.lang.reflect.InvocationTargetException;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityStatus;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.EnumChatVisibility;
import net.minecraft.world.level.block.entity.TileEntitySign;
import net.minecraft.world.level.block.entity.TileEntitySkull;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftSkull;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
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
        return new SkinSettings((Integer) handle.getDataWatcher().get(PA.getClientSettings()));
    }

    @Nonnull
    @Override
    public ChatSetting getChatSetting(@Nonnull Player p) {
        CraftPlayer pl = (CraftPlayer) p;
        EntityPlayer handle = pl.getHandle();
        EnumChatVisibility nmsVisibility = handle.getChatFlags();
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
            org.bukkit.craftbukkit.v1_17_R1.CraftWorld cw = (org.bukkit.craftbukkit.v1_17_R1.CraftWorld) world;
            WorldServer handle = cw.getHandle();
            getMethod(PlayerChunkMap.class, "setViewDistance", int.class).invoke(handle.getChunkProvider().a, distance);
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
        cp.getHandle().b.sendPacket((Packet<?>) packet);
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
        NBTTagCompound tag = tileEntity.Z_();
        BlockPosition bp = tileEntity.getPosition();
        if (gameProfile == null) {
            tag.remove("SkullOwner");
        } else {
            NBTTagCompound gameProfileTag = new NBTTagCompound();
            GameProfileSerializer.serialize(gameProfileTag, gameProfile);
            tag.set("SkullOwner", gameProfileTag);
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
    public void openSign(@Nonnull Player player, @Nonnull Sign sign) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer nmsPlayer = craftPlayer.getHandle();

        TileEntitySign nmsSign;
        try {
            nmsSign = (TileEntitySign) getMethod(sign, "getTileEntity").invoke(sign);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        nmsSign.a(true); // Set isEditable to true
        nmsSign.a(nmsPlayer.getUniqueID()); // set playerWhoMayEdit to the player editing the sign
        nmsPlayer.openSign(nmsSign);
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
            ei.getNavigation().a(x, y, z, 1.0D);
        }
    }

    private abstract static class PA extends EntityHuman {

        private PA() {
            super(null, null, 0.0f, null);
        }

        public static DataWatcherObject<?> getClientSettings() {
            return bP;
        }

        public static DataWatcherObject<?> getMainHandSetting() {
            return bQ;
        }
    }
}
