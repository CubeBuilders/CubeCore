package io.siggi.cubecore.bungee.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BrigadierVersion {
    public int[] protocolVersions;
    public String[] minecraftVersions;
    public String[] argumentTypes;
    final transient Map<String, Integer> typeToInt = new HashMap<>();
    final transient Map<String, ArgumentType<?>> cache = new ConcurrentHashMap<>();
}
