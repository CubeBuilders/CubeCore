package io.siggi.cubecore;

import com.google.gson.GsonBuilder;

import java.util.logging.Logger;

public interface CubeCorePlugin {
    void registerTypeAdapters(GsonBuilder builder);
    Logger getLogger();
}
