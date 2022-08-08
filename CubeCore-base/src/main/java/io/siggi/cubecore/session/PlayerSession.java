package io.siggi.cubecore.session;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class PlayerSession {
    private static final ReentrantReadWriteLock rootLock = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock rootRead = rootLock.readLock();
    private static final ReentrantReadWriteLock.WriteLock rootWrite = rootLock.writeLock();
    private static final Map<UUID, PlayerSession> sessions = new HashMap<>();
    private static final ReentrantReadWriteLock creatorLock = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock creatorRead = creatorLock.readLock();
    private static final ReentrantReadWriteLock.WriteLock creatorWrite = creatorLock.writeLock();
    private static final Map<Class<? extends SessionData>, Function<UUID, ? extends SessionData>> dataCreators = new HashMap<>();
    private final ReentrantReadWriteLock sessionLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock sessionRead = sessionLock.readLock();
    private final ReentrantReadWriteLock.WriteLock sessionWrite = sessionLock.writeLock();
    private final Map<Class<? extends SessionData>, SessionData> sessionData = new HashMap<>();
    private final UUID uuid;

    private PlayerSession(UUID uuid) {
        this.uuid = uuid;
    }

    public static <T extends SessionData> T get(UUID uuid, Class<T> dataClass) {
        return get(uuid).get(dataClass);
    }

    public static PlayerSession get(UUID uuid) {
        rootRead.lock();
        try {
            PlayerSession session = sessions.get(uuid);
            if (session != null)
                return session;
        } finally {
            rootRead.unlock();
        }
        rootWrite.lock();
        try {
            PlayerSession session = sessions.get(uuid);
            if (session == null)
                sessions.put(uuid, session = new PlayerSession(uuid));
            return session;
        } finally {
            rootWrite.unlock();
        }
    }

    public static void clear(UUID uuid) {
        rootWrite.lock();
        try {
            sessions.remove(uuid);
        } finally {
            rootWrite.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends SessionData> Function<UUID, T> getCreator(Class<T> dataClass) {
        creatorRead.lock();
        try {
            Function<UUID, ? extends SessionData> creator = dataCreators.get(dataClass);
            if (creator != null)
                return (Function<UUID, T>) creator;
        } finally {
            creatorRead.unlock();
        }
        creatorWrite.lock();
        try {
            Function<UUID, ? extends SessionData> creator = dataCreators.get(dataClass);
            if (creator == null)
                dataCreators.put(dataClass, creator = findCreator(dataClass));
            return (Function<UUID, T>) creator;
        } finally {
            creatorWrite.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends SessionData> Function<UUID, T> findCreator(Class<T> dataClass) {
        try {
            Method creatorMethod = dataClass.getDeclaredMethod("getCreator");
            Function<UUID, T> creator = (Function<UUID, T>) creatorMethod.invoke(null);
            if (creator != null)
                return creator;
        } catch (Exception e) {
        }
        try {
            Constructor<T> constructor = dataClass.getDeclaredConstructor(UUID.class);
            return (uuid) -> {
                try {
                    return constructor.newInstance(uuid);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("No constructor(UUID) and no getCreator() found in " + dataClass.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends SessionData> T get(Class<T> dataClass) {
        sessionRead.lock();
        try {
            T data = (T) sessionData.get(dataClass);
            if (data != null)
                return data;
        } finally {
            sessionRead.unlock();
        }
        sessionWrite.lock();
        try {
            T data = (T) sessionData.get(dataClass);
            if (data == null)
                sessionData.put(dataClass, data = getCreator(dataClass).apply(uuid));
            return data;
        } finally {
            sessionWrite.unlock();
        }
    }
}
