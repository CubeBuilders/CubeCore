package io.siggi.cubecore.bedrockapi;

import java.util.UUID;

interface BedrockDeviceApi {
    boolean isOnBedrock(UUID player);

    BedrockDeviceInfo.BedrockDevice getDevice(UUID player);

    BedrockDeviceInfo.BedrockInputMode getInputMode(UUID player);

    BedrockDeviceInfo.BedrockUiProfile getUiProfile(UUID player);
}
