package io.siggi.cubecore.userinfo;

import java.util.UUID;

public final class UserInfo {
    private final UUID uuid;
    private final String username;
    private final String texturesPayload;
    private final String texturesSignature;

    public UserInfo(UUID uuid, String username, String texturesPayload, String texturesSignature) {
        this.uuid = uuid;
        this.username = username;
        this.texturesPayload = texturesPayload;
        this.texturesSignature = texturesSignature;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getTexturesPayload() {
        return texturesPayload;
    }

    public String getTexturesSignature() {
        return texturesSignature;
    }
}
