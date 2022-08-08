package io.siggi.cubecore;

import com.google.gson.GsonBuilder;

public interface CubeCorePlugin {
    void registerTypeAdapters(GsonBuilder builder);
}
