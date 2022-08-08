package io.siggi.cubecore.usercache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class UserCache {
    private final File root;
    private final NameCache names;
    private final NameCache nicks;
    private final LastLogins lastLogins;
    private final TextureCache textures;

    // TODO: Make this configurable
    private final String nickPrefix = "*";

    public UserCache(File root) {
        this.root = root;
        this.names = new NameCache(new File(root, "names.txt"));
        this.nicks = new NameCache(new File(root, "nicks.txt"));
        this.lastLogins = new LastLogins(new File(root, "lastlogins.txt"));
        this.textures = new TextureCache(new File(root, "textures"));
    }

    public String getName(UUID uuid) {
        NameCache.Entry nameEntry = names.getByUuid(uuid);
        if (nameEntry != null)
            return nameEntry.getName();
        return null;
    }

    public String getNameOrNick(UUID uuid) {
        NameCache.Entry nickEntry = nicks.getByUuid(uuid);
        if (nickEntry != null) {
            NameCache.Entry byName = names.getByName(nickEntry.getName());
            if (byName == null || byName.getUUID().equals(nickEntry.getUUID())) {
                return nickPrefix + nickEntry.getName();
            }
        }
        NameCache.Entry nameEntry = names.getByUuid(uuid);
        if (nameEntry != null)
            return nameEntry.getName();
        return null;
    }

    public UUID getUUID(String name) {
        if (name.startsWith(nickPrefix)) name = name.substring(nickPrefix.length());
        NameCache.Entry nameEntry = names.getByName(name);
        if (nameEntry != null)
            return nameEntry.getUUID();
        NameCache.Entry nickEntry = nicks.getByName(name);
        if (nickEntry != null)
            return nickEntry.getUUID();
        return null;
    }

    public List<String> autocomplete(String namePrefix, int limit) {
        ArrayList<NameCache.Entry> list = new ArrayList<>();
        names.getPlayersWithNamesStartingWith(namePrefix, list);
        nicks.getPlayersWithNamesStartingWith(namePrefix, list);
        list.sort(lastLogins.sortByRecentFirst());
        int count = Math.min(limit, list.size());
        List<String> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(list.get(i).getName());
        }
        return result;
    }

    public NameCache getNames() {
        return names;
    }

    public NameCache getNicks() {
        return nicks;
    }

    public LastLogins getLastLogins() {
        return lastLogins;
    }

    public TextureCache getTextures() {
        return textures;
    }
}
