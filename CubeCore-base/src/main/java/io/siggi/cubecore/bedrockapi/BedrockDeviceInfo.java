package io.siggi.cubecore.bedrockapi;

import java.util.UUID;

public class BedrockDeviceInfo {
    // <editor-fold desc="Setup" defaultstate="collapsed">
    private BedrockDeviceInfo() {
    }

    private static final BedrockDeviceApi apiInterface;

    static {
        BedrockDeviceApi apiInt = null;
        if (apiInt == null) {
            try {
                if (BedrockDeviceApiFloodgate.isFloodgateAvailable()) {
                    apiInt = new BedrockDeviceApiFloodgate();
                }
            } catch (Throwable t) {
            }
        }
        if (apiInt == null) {
            apiInt = new BedrockDeviceApiNull();
        }
        apiInterface = apiInt;
    }
    // </editor-fold>

    public static boolean isOnBedrock(UUID player) {
        return apiInterface.isOnBedrock(player);
    }

    public static BedrockDevice getDevice(UUID player) {
        return apiInterface.getDevice(player);
    }

    public static BedrockInputMode getInputMode(UUID player) {
        return apiInterface.getInputMode(player);
    }

    public static BedrockUiProfile getUiProfile(UUID player) {
        return apiInterface.getUiProfile(player);
    }

    // <editor-fold desc="Enums" defaultstate="collapsed">
    public enum BedrockDevice {
        UNKNOWN("Unknown"),
        GOOGLE("Android"),
        IOS("iOS"),
        OSX("macOS"),
        AMAZON("Amazon"),
        GEARVR("Gear VR"),
        HOLOLENS("Hololens"),
        UWP("Windows 10"),
        WIN32("Windows x86"),
        DEDICATED("Dedicated"),
        TVOS("Apple TV"),
        PS4("PlayStation"),
        NX("Nintendo Switch"),
        XBOX("Xbox"),
        WINDOWS_PHONE("Windows Phone");

        private final String name;

        public String getName() {
            return name;
        }

        private BedrockDevice(String name) {
            this.name = name;
        }
    }

    public enum BedrockInputMode {
        UNKNOWN("Unknown", true),
        KEYBOARD_MOUSE("Keyboard & Mouse", true),
        TOUCH("Touch", false),
        CONTROLLER("Controller", true),
        VR("VR Headset", true);

        private final String name;
        private final boolean inventoryInterfaceSafe;

        public String getName() {
            return name;
        }

        public boolean isInventoryInterfaceSafe() {
            return inventoryInterfaceSafe;
        }

        private BedrockInputMode(String name, boolean inventoryInterfaceSafe) {
            this.name = name;
            this.inventoryInterfaceSafe = inventoryInterfaceSafe;
        }
    }

    public enum BedrockUiProfile {
        CLASSIC("Classic"),
        POCKET("Pocket");

        private final String name;

        public String getName() {
            return name;
        }

        private BedrockUiProfile(String name) {
            this.name = name;
        }
    }
    // </editor-fold>
}
