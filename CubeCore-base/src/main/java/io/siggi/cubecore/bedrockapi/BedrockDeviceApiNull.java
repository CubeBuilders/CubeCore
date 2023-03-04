package io.siggi.cubecore.bedrockapi;

import java.util.UUID;

class BedrockDeviceApiNull implements BedrockDeviceApi {
    public boolean isOnBedrock(UUID player) {
        return false;
    }

    public BedrockDeviceInfo.BedrockDevice getDevice(UUID player) {
        return BedrockDeviceInfo.BedrockDevice.UNKNOWN;
    }

    public BedrockDeviceInfo.BedrockInputMode getInputMode(UUID player) {
        return BedrockDeviceInfo.BedrockInputMode.UNKNOWN;
    }

    public BedrockDeviceInfo.BedrockUiProfile getUiProfile(UUID player) {
        return BedrockDeviceInfo.BedrockUiProfile.CLASSIC;
    }
}
