package io.siggi.cubecore.usercache;

import io.siggi.cubecore.io.LineReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class LastLogins {
    private final File dataFile;
    private final Map<UUID, Entry> entries = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final Comparator<NameCache.Entry> sortByRecentFirst = (a, b) -> {
        long aTime = get(a.getUUID());
        long bTime = get(b.getUUID());
        if (aTime > bTime) {
            return -1;
        } else if (aTime < bTime) {
            return 1;
        } else {
            return 0;
        }
    };
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public LastLogins(File file) {
        this.dataFile = file;
        loadData();
    }

    public long get(UUID uuid) {
        readLock.lock();
        try {
            Entry entry = entries.get(uuid);
            if (entry == null)
                return 0L;
            return entry.time;
        } finally {
            readLock.unlock();
        }
    }

    public void set(UUID uuid, long time) {
        writeLock.lock();
        try {
            Entry entry = entries.get(uuid);
            if (entry == null) {
                long length = dataFile.length();
                String line = uuid.toString() + "=" + longToPaddedString(time);
                long offset = line.indexOf("=") + 1 + length;
                try (FileOutputStream out = new FileOutputStream(dataFile, true)) {
                    out.write(line.getBytes());
                    out.write(0x0A);
                }
                entry = new Entry(uuid, offset, time);
                entries.put(uuid, entry);
            } else {
                entry.time = time;
                try (RandomAccessFile raf = new RandomAccessFile(dataFile, "rw")) {
                    raf.seek(entry.offset);
                    raf.write(longToPaddedString(time).getBytes());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            writeLock.unlock();
        }
    }

    public Comparator<NameCache.Entry> sortByRecentFirst() {
        return sortByRecentFirst;
    }

    private final String longToPaddedString(long value) {
        return longToPaddedString(value, 16);
    }

    private final String longToPaddedString(long value, int count) {
        String string = Long.toString(value);
        int missing = count - string.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < missing; i++) {
            sb.append("0");
        }
        sb.append(string);
        return sb.toString();
    }

    private void loadData() {
        try (LineReader reader = new LineReader(new FileInputStream(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int eqPos = line.indexOf("=");
                if (eqPos == -1)
                    continue;
                try {
                    UUID uuid = UUID.fromString(line.substring(0, eqPos));
                    long time = Long.parseLong(line.substring(eqPos + 1));
                    long offset = reader.getBeginningOfLine() + ((long) eqPos) + 1L;
                    entries.put(uuid, new Entry(uuid, offset, time));
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }
    }

    private class Entry {
        private final UUID uuid;
        private final long offset;
        private long time;

        private Entry(UUID uuid, long offset, long time) {
            this.uuid = uuid;
            this.offset = offset;
            this.time = time;
        }
    }
}
