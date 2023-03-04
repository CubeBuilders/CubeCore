package io.siggi.cubecore.session;

import java.util.UUID;
import java.util.function.Function;

public class CubeCoreSession {
    private final UUID player;
    public String clientBrand;

    public CubeCoreSession(UUID player) {
        this.player = player;
    }

    public static Function<UUID, CubeCoreSession> getCreator() {
        return CubeCoreSession::new;
    }
}
