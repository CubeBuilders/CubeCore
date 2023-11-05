package io.siggi.cubecore.userinfo;

import io.siggi.cubecore.AsyncValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserDatabase {
    AsyncValue<UserInfo> getByUniqueId(UUID uuid);
    AsyncValue<UserInfo> getByName(String name);
    AsyncValue<Map<UUID,UserInfo>> getMultipleByUniqueId(Collection<UUID> uuids);
    AsyncValue<List<UserInfo>> autocomplete(String name);
    void storeToCache(UUID uuid, String name, String texturesPayload, String texturesSignature);
}
