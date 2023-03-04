package io.siggi.cubecore.bedrockapi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.util.DeviceOs;
import org.geysermc.floodgate.util.InputMode;
import org.geysermc.floodgate.util.UiProfile;

class BedrockDeviceApiFloodgate implements BedrockDeviceApi {
    static boolean isFloodgateAvailable() {
        try {
            FloodgateApi.getInstance();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private final Map<DeviceOs, BedrockDeviceInfo.BedrockDevice> deviceMapping = new HashMap<>();
    private final Map<InputMode, BedrockDeviceInfo.BedrockInputMode> inputModeMapping = new HashMap<>();
    private final Map<UiProfile, BedrockDeviceInfo.BedrockUiProfile> uiProfileMapping = new HashMap<>();

    BedrockDeviceApiFloodgate() {
        deviceMapping.put(DeviceOs.UNKNOWN, BedrockDeviceInfo.BedrockDevice.UNKNOWN);
        deviceMapping.put(DeviceOs.GOOGLE, BedrockDeviceInfo.BedrockDevice.GOOGLE);
        deviceMapping.put(DeviceOs.IOS, BedrockDeviceInfo.BedrockDevice.IOS);
        deviceMapping.put(DeviceOs.OSX, BedrockDeviceInfo.BedrockDevice.OSX);
        deviceMapping.put(DeviceOs.AMAZON, BedrockDeviceInfo.BedrockDevice.AMAZON);
        deviceMapping.put(DeviceOs.GEARVR, BedrockDeviceInfo.BedrockDevice.GEARVR);
        deviceMapping.put(DeviceOs.HOLOLENS, BedrockDeviceInfo.BedrockDevice.HOLOLENS);
        deviceMapping.put(DeviceOs.UWP, BedrockDeviceInfo.BedrockDevice.UWP);
        deviceMapping.put(DeviceOs.WIN32, BedrockDeviceInfo.BedrockDevice.WIN32);
        deviceMapping.put(DeviceOs.DEDICATED, BedrockDeviceInfo.BedrockDevice.DEDICATED);
        deviceMapping.put(DeviceOs.TVOS, BedrockDeviceInfo.BedrockDevice.TVOS);
        deviceMapping.put(DeviceOs.PS4, BedrockDeviceInfo.BedrockDevice.PS4);
        deviceMapping.put(DeviceOs.NX, BedrockDeviceInfo.BedrockDevice.NX);
        deviceMapping.put(DeviceOs.XBOX, BedrockDeviceInfo.BedrockDevice.XBOX);
        deviceMapping.put(DeviceOs.WINDOWS_PHONE, BedrockDeviceInfo.BedrockDevice.WINDOWS_PHONE);

        inputModeMapping.put(InputMode.UNKNOWN, BedrockDeviceInfo.BedrockInputMode.UNKNOWN);
        inputModeMapping.put(InputMode.KEYBOARD_MOUSE, BedrockDeviceInfo.BedrockInputMode.KEYBOARD_MOUSE);
        inputModeMapping.put(InputMode.TOUCH, BedrockDeviceInfo.BedrockInputMode.TOUCH);
        inputModeMapping.put(InputMode.CONTROLLER, BedrockDeviceInfo.BedrockInputMode.CONTROLLER);
        inputModeMapping.put(InputMode.VR, BedrockDeviceInfo.BedrockInputMode.VR);

        uiProfileMapping.put(UiProfile.CLASSIC, BedrockDeviceInfo.BedrockUiProfile.CLASSIC);
        uiProfileMapping.put(UiProfile.POCKET, BedrockDeviceInfo.BedrockUiProfile.POCKET);
    }

    @Override
    public boolean isOnBedrock(UUID player) {
        return FloodgateApi.getInstance().isFloodgatePlayer(player);
    }

    @Override
    public BedrockDeviceInfo.BedrockDevice getDevice(UUID player) {
        try {
            return deviceMapping.getOrDefault(
                FloodgateApi.getInstance().getPlayer(player).getDeviceOs(),
                BedrockDeviceInfo.BedrockDevice.UNKNOWN
            );
        } catch (Exception e) {
            return BedrockDeviceInfo.BedrockDevice.UNKNOWN;
        }
    }

    @Override
    public BedrockDeviceInfo.BedrockInputMode getInputMode(UUID player) {
        try {
            return inputModeMapping.getOrDefault(
                FloodgateApi.getInstance().getPlayer(player).getInputMode(),
                BedrockDeviceInfo.BedrockInputMode.UNKNOWN
            );
        } catch (Exception e) {
            return BedrockDeviceInfo.BedrockInputMode.UNKNOWN;
        }
    }

    @Override
    public BedrockDeviceInfo.BedrockUiProfile getUiProfile(UUID player) {
        try {
            return uiProfileMapping.getOrDefault(
                FloodgateApi.getInstance().getPlayer(player).getUiProfile(),
                BedrockDeviceInfo.BedrockUiProfile.CLASSIC
            );
        } catch (Exception e) {
            return BedrockDeviceInfo.BedrockUiProfile.CLASSIC;
        }
    }
}
