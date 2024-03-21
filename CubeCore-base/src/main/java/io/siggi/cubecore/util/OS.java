package io.siggi.cubecore.util;

public enum OS {
    MACOS, LINUX, WINDOWS, OTHER;
    public static final OS CURRENT_OS;

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) {
            CURRENT_OS = MACOS;
        } else if (osName.contains("windows")) {
            CURRENT_OS = WINDOWS;
        } else if (osName.contains("linux")) {
            CURRENT_OS = LINUX;
        } else {
            CURRENT_OS = OTHER;
        }
    }
}
