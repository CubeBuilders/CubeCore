package io.siggi.cubecore.nms;

public class SkinSettings {

    public static final int SKIN_CAPE = 0x1;
    public static final int SKIN_JACKET = 0x2;
    public static final int SKIN_LEFT_SLEEVE = 0x4;
    public static final int SKIN_RIGHT_SLEEVE = 0x8;
    public static final int SKIN_LEFT_PANTS_LEG = 0x10;
    public static final int SKIN_RIGHT_PANTS_LEG = 0x20;
    public static final int SKIN_HAT = 0x40;
    private final int settings;

    public SkinSettings(int settings) {
        this.settings = settings;
    }

    public int getRawSettings() {
        return settings;
    }

    public boolean isCapeOn() {
        return (settings & SKIN_CAPE) != 0;
    }

    public boolean isJacketOn() {
        return (settings & SKIN_JACKET) != 0;
    }

    public boolean isLeftSleeveOn() {
        return (settings & SKIN_LEFT_SLEEVE) != 0;
    }

    public boolean isRightSleeveOn() {
        return (settings & SKIN_RIGHT_SLEEVE) != 0;
    }

    public boolean isLeftPantsLegOn() {
        return (settings & SKIN_LEFT_PANTS_LEG) != 0;
    }

    public boolean isRightPantsLegOn() {
        return (settings & SKIN_RIGHT_PANTS_LEG) != 0;
    }

    public boolean isHatOn() {
        return (settings & SKIN_HAT) != 0;
    }
}
