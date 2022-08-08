package io.siggi.cubecore.usercache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;

/**
 * Caches usernames for UUIDs.
 *
 * @author Siggi
 */
public final class NameCache {

    private final File dataFile;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    private final Map<UUID, Entry> byUuid = new HashMap<>();
    private final Map<String, Entry> byName = new HashMap<>();
    private final TreeMap<String, Entry> byNameSorted = new TreeMap<>();
    private boolean loadingUUIDs = false;
    private int obsoleteLines = 0;
    public NameCache(File dataFile) {
        this.dataFile = dataFile;
        loadData();
    }

    /**
     * Get a player's name from their UUID.
     *
     * @param uuid the player's UUID
     * @return the player's name
     */
    @Nullable
    public Entry getByUuid(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        readLock.lock();
        try {
            return byUuid.get(uuid);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Get a player's UUID from their name.
     *
     * @param name the player's name
     * @return the player's UUID
     */
    @Nullable
    public Entry getByName(String name) {
        if (name == null) {
            return null;
        }
        readLock.lock();
        try {
            return byName.get(name.toLowerCase());
        } finally {
            readLock.unlock();
        }
    }

    public <T extends Collection<Entry>> T getPlayersWithNamesStartingWith(String startingWith, T collection) {
        readLock.lock();
        try {
            startingWith = startingWith.toLowerCase();
            NavigableMap<String, Entry> map;
            if (startingWith.isEmpty()) {
                map = byNameSorted;
            } else {
                String startingWith2 = startingWith.substring(0, startingWith.length() - 1) + (char) (startingWith.charAt(startingWith.length() - 1) + 1);
                map = byNameSorted.subMap(startingWith, true, startingWith2, false);
            }
            for (Map.Entry<String, Entry> entry : map.entrySet()) {
                String n = entry.getKey();
                Entry cacheEntry = entry.getValue();
                if (n.startsWith(startingWith)) {
                    collection.add(cacheEntry);
                }
            }
            return collection;
        } finally {
            readLock.unlock();
        }
    }

    public void store(UUID uuid, String name) {
        if (name == null) {
            name = "";
        }
        String nameLowercase = name.toLowerCase();
        if (!loadingUUIDs) {
            writeLock.lock();
        }
        try {
            Entry uuidCheck = getByName(name);
            Entry nameCheck = getByUuid(uuid);
            if ((uuidCheck != null && uuidCheck.getUUID().equals(uuid)) && (nameCheck != null && nameCheck.getName().equals(name))) {
                return;
            }
            Entry oldName = byUuid.get(uuid);
            Entry oldUUIDForNewName = name.isEmpty() ? null : byName.get(nameLowercase);
            if (oldName != null) {
                obsoleteLines += 1;
                if (oldName.getName() != null) {
                    String oldNameLowercase = oldName.getName().toLowerCase();
                    byName.remove(oldNameLowercase);
                    byNameSorted.remove(oldNameLowercase);
                }
            }
            if (oldUUIDForNewName != null) {
                byUuid.remove(oldUUIDForNewName.getUUID());
            }
            if (name.isEmpty()) {
                byUuid.remove(uuid);
            } else {
                Entry newEntry;
                if (oldName != null) {
                    newEntry = oldName;
                    newEntry.setName(name);
                } else {
                    newEntry = new Entry(uuid, name);
                }
                byUuid.put(uuid, newEntry);
                byName.put(nameLowercase, newEntry);
                byNameSorted.put(nameLowercase, newEntry);
            }
            if (!loadingUUIDs) {
                try (FileOutputStream out = new FileOutputStream(dataFile, true)) {
                    byte[] data = (uuid.toString() + "=" + name + "\n").getBytes(StandardCharsets.UTF_8);
                    out.write(data);
                } catch (Exception e) {
                }
            }
        } finally {
            if (!loadingUUIDs) {
                writeLock.unlock();
            }
        }
    }

    private void loadData() {
        writeLock.lock();
        try {
            loadingUUIDs = true;
            byUuid.clear();
            byName.clear();
            obsoleteLines = 0;
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                int equalPosition = line.indexOf("=");
                if (equalPosition == -1)
                    continue;
                String key = line.substring(0, equalPosition);
                String val = line.substring(equalPosition + 1);
                try {
                    store(UUID.fromString(key), val);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        } finally {
            loadingUUIDs = false;
            writeLock.unlock();
        }
    }

    public class Entry {
        private final UUID uuid;
        private String name;

        private Entry(UUID uuid, String name) {
            if (uuid == null || name == null) throw new NullPointerException();
            this.uuid = uuid;
            this.name = name;
        }

        public UUID getUUID() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }
    }
}
