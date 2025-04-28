package io.siggi.cubecore.nms;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public abstract class NMSUtil {

    private static NMSUtil util = null;

    /**
     * Get NMSUtil, which may return a no-op implementation if it is not supported on the current server version.
     *
     * @return the NMSUtil
     */
    @Nonnull
    public static NMSUtil get() {
        if (util == null) {
            try {
                Class clazz = Class.forName("io.siggi.cubecore.nms." + getVersion() + ".NMSUtil");
                util = (NMSUtil) clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                util = new NullNMSUtil();
            }
        }
        return util;
    }

    private static final Map<String, String> nmsVersions = new HashMap<>();
    private static final String latestNmsVersion;
    static {
        nmsVersions.put("1.20.5", "v1_20_R4");
        nmsVersions.put("1.20.6", "v1_20_R4");
        nmsVersions.put("1.21", "v1_21_R1");
        nmsVersions.put("1.21.1", "v1_21_R1");
        nmsVersions.put("1.21.2", "v1_21_R2");
        nmsVersions.put("1.21.3", "v1_21_R2");
        nmsVersions.put("1.21.4", "v1_21_R3");
        nmsVersions.put("1.21.5", "v1_21_R4");
        latestNmsVersion = "v1_21_R4";
    }

    private static String getVersion() {
        String name = Bukkit.getServer().getClass().getName();
        String version = name.substring(name.indexOf(".v") + 1);
        version = version.substring(0, version.indexOf("."));
        if (!version.startsWith("v")) {
            return nmsVersions.getOrDefault(Bukkit.getMinecraftVersion(), latestNmsVersion);
        }
        return version;
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    // <editor-fold desc="Reflection methods" defaultstate="collapsed">
    protected static Method getMethod(Class<?> clazz, String name, Class<?>... paramTypes) throws NoSuchMethodException {
        Method method = clazz.getDeclaredMethod(name, paramTypes);
        method.setAccessible(true);
        return method;
    }

    protected static Method getMethod(Object object, String name, Class<?>... paramTypes) throws NoSuchMethodException {
        Class<?> clazz = object.getClass();
        NoSuchMethodException nsme = null;
        while (true) {
            try {
                Method m = clazz.getDeclaredMethod(name, paramTypes);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException e) {
                if (nsme == null)
                    nsme = e;
            }
            if (clazz == Object.class) {
                break;
            }
            clazz = clazz.getSuperclass();
        }
        throw nsme;
    }

    protected static Field getField(Class<?> clazz, String field) throws NoSuchFieldException {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        return f;
    }

    protected static Field getField(Object object, String field) throws NoSuchFieldException {
        Class<?> clazz = object.getClass();
        NoSuchFieldException nsfe = null;
        while (true) {
            try {
                Field f = clazz.getDeclaredField(field);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                if (nsfe == null)
                    nsfe = e;
            }
            if (clazz == Object.class) {
                break;
            }
            clazz = clazz.getSuperclass();
        }
        throw nsfe;
    }
    // </editor-fold>

    /**
     * Get BrigadierUtil which may be null if it is not supported on the current server version.
     *
     * @return BrigadierUtil or null if not supported on the current server version.
     */
    @Nullable
    public abstract BrigadierUtil getBrigadierUtil();

    /**
     * Get the skin settings for a player.
     *
     * @param p the skin settings to get
     * @return the skin settings
     */
    @Nonnull
    public abstract SkinSettings getSkinSettings(@Nonnull Player p);

    /**
     * Get the chat visibility setting for a player.
     *
     * @param p the player to get the chat visibility setting for
     * @return the chat visibility setting
     */
    @Nonnull
    public abstract ChatSetting getChatSetting(@Nonnull Player p);

    /**
     * Get the GameProfile for a player.
     *
     * @param p the player to get a GameProfile for
     * @return the GameProfile
     */
    @Nullable
    public abstract GameProfile getGameProfile(@Nonnull Player p);

    /**
     * Set the render distance for a world.
     *
     * @param world    the world to change the render distance for
     * @param distance the render distance to set
     */
    public abstract void setRenderDistance(@Nonnull org.bukkit.World world, int distance);

    /**
     * Send a packet to a client.
     *
     * @param p      the player of the client you want to send the packet to
     * @param packet the packet you want to send
     */
    public abstract void sendPacket(@Nonnull Player p, @Nullable Object packet);

    /**
     * Create a packet that changes the profile of a player head.
     *
     * @param skull       the player head to change
     * @param gameProfile the GameProfile to change it to
     * @return the packet that you should pass to {@link NMSUtil#sendPacket(Player, Object)}
     */
    @Nullable
    public abstract Object createPacketSetSkullProfile(@Nonnull Skull skull, @Nullable GameProfile gameProfile);

    /**
     * Determine if an entity is a hostile entity.
     *
     * @param entity the entity to check
     * @return true if the entity is hostile, false otherwise
     */
    public abstract boolean isHostile(@Nonnull LivingEntity entity);

    /**
     * Get the ping for a player.
     *
     * @param p the player to get ping for
     * @return the ping
     */
    public int getPing(@Nonnull Player p) {
        // Bukkit versions 1.16.5 and newer implement a getPing().
        return p.getPing();
    }

    /**
     * Get the language a player has set their client to, which may be null.
     *
     * @param p the player to get the language for
     * @return the language
     */
    @Nonnull
    public String getLocale(@Nonnull Player p) {
        // Bukkit versions 1.12 and newer implement a getLocale().
        return p.getLocale();
    }

    /**
     * Open the sign editor for a player.
     *
     * @param p     the player to open the sign editor for
     * @param block the sign the player will be editing
     */
    public void openSign(@Nonnull Player p, @Nonnull Block block) {
        BlockState state = block.getState();
        if (!(state instanceof Sign)) {
            throw new IllegalArgumentException("Passed block is not a sign.");
        }
        openSign(p, (Sign) state);
    }

    /**
     * Open the sign editor for a player.
     *
     * @param p    the player to open the sign editor for
     * @param sign the sign the player will be editing
     */
    public void openSign(@Nonnull Player p, @Nonnull Sign sign) {
        // Bukkit versions 1.18 and newer implement an openSign() method.
        p.openSign(sign);
    }

    /**
     * Create an entity status packet for an entity.
     *
     * @param entity the entity to create the packet for
     * @param status the status for the entity
     * @return the entity status packet
     */
    public abstract Object createEntityStatusPacket(@Nonnull Entity entity, int status);

    /**
     * Set the client side op level for a player.
     *
     * @param p       The player to set a client side op level for.
     * @param opLevel The op level to set.
     */
    public void setClientSideOpLevel(@Nonnull Player p, int opLevel) {
        sendPacket(p, createEntityStatusPacket(p, clamp(opLevel, 0, 4) + 24));
    }

    /**
     * Set the walk destination for an entity to path find to.
     *
     * @param entity the entity to set a walk destination for
     * @param x      the x coordinate of the destination
     * @param y      the y coordinate of the destination
     * @param z      the z coordinate of the destination
     */
    public abstract void setWalkDestination(@Nonnull LivingEntity entity, double x, double y, double z);

    public AuthLibProperty wrapProperty(Property property) {
        return new AuthLibProperty(property.name(), property.value(), property.signature());
    }
}
