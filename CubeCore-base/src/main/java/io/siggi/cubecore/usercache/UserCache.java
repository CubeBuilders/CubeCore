package io.siggi.cubecore.usercache;

import io.siggi.cubecore.AsyncValue;
import io.siggi.cubecore.userinfo.UserDatabase;
import io.siggi.cubecore.userinfo.UserInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class UserCache implements UserDatabase {
    private final File root;
    private final NameCache names;
    private final LastLogins lastLogins;
    private final TextureCache textures;

    public UserCache(File root) {
        this.root = root;
        if (!root.exists()) {
            root.mkdirs();
        }
        this.names = new NameCache(new File(root, "names.txt"));
        this.lastLogins = new LastLogins(new File(root, "lastlogins.txt"));
        this.textures = new TextureCache(new File(root, "textures"));
    }

    public String getName(UUID uuid) {
        NameCache.Entry nameEntry = names.getByUuid(uuid);
        if (nameEntry != null)
            return nameEntry.getName();
        return null;
    }

    @Deprecated
    public String getNameOrNick(UUID uuid) {
        return getName(uuid);
    }

    public UUID getUUID(String name) {
        NameCache.Entry nameEntry = names.getByName(name);
        if (nameEntry != null)
            return nameEntry.getUUID();
        return null;
    }

    public List<String> autocomplete(String namePrefix, int limit) {
        List<UserInfo> userInfos = autocompleteUserInfo(namePrefix, limit);
        List<String> names = new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            names.add(userInfo.getUsername());
        }
        return names;
    }

    private List<UserInfo> autocompleteUserInfo(String namePrefix, int limit) {
        ArrayList<NameCache.Entry> list = new ArrayList<>();
        names.getPlayersWithNamesStartingWith(namePrefix, list);
        list.sort(lastLogins.sortByRecentFirst());
        int count = Math.min(limit, list.size());
        List<UserInfo> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(getUserInfo(list.get(i).getUUID()));
        }
        return result;
    }

    public NameCache getNames() {
        return names;
    }

    @Deprecated
    public NameCache getNicks() {
        throw new UnsupportedOperationException();
    }

    public LastLogins getLastLogins() {
        return lastLogins;
    }

    public TextureCache getTextures() {
        return textures;
    }

    private UserInfo getUserInfo(UUID uuid) {
        NameCache.Entry byUuid = names.getByUuid(uuid);
        if (byUuid == null) return null;
        String name = byUuid.getName();
        TextureCache.Entry entry = textures.get(uuid);
        String texturesPayload = entry == null ? null : entry.getPayload();
        String texturesSignature = entry == null ? null : entry.getSignature();
        return new UserInfo(uuid, name, texturesPayload, texturesSignature);
    }

    @Override
    public AsyncValue<UserInfo> getByUniqueId(UUID uuid) {
        return AsyncValue.of(CompletableFuture.completedFuture(getUserInfo(uuid)));
    }

    @Override
    public AsyncValue<UserInfo> getByName(String name) {
        UUID uuid = getUUID(name);
        if (uuid == null) return AsyncValue.of(CompletableFuture.completedFuture(null));
        return getByUniqueId(uuid);
    }

    @Override
    public AsyncValue<Map<UUID, UserInfo>> getMultipleByUniqueId(Collection<UUID> uuids) {
        Map<UUID, UserInfo> map = new HashMap<>();
        for (UUID uuid : uuids) {
            UserInfo userInfo = getUserInfo(uuid);
            if (userInfo != null) {
                map.put(uuid, userInfo);
            }
        }
        return AsyncValue.of(CompletableFuture.completedFuture(map));
    }

    @Override
    public AsyncValue<List<UserInfo>> autocomplete(String name) {
        // TODO: Implement this
        return AsyncValue.of(CompletableFuture.completedFuture(new ArrayList<>()));
    }

    @Override
    public void storeToCache(UUID uuid, String name, String texturesPayload, String texturesSignature) {
        if (uuid == null || name == null) return;
        names.store(uuid, name);
        if (texturesPayload == null || texturesSignature == null) return;
        textures.store(uuid, texturesPayload, texturesSignature);
    }
}
