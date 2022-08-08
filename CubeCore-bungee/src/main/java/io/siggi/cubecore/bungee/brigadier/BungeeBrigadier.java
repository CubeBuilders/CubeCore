package io.siggi.cubecore.bungee.brigadier;

import com.google.gson.Gson;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.protocol.ProtocolConstants;

public class BungeeBrigadier {

    private BungeeBrigadier() {
    }

    public static final Command DUMMY_COMMAND = (context) -> {
        return 0;
    };

    private static final Map<Integer, BrigadierVersion> versionMap = new HashMap<>();

    static {
        try {
            try (InputStream typeStream = BungeeBrigadier.class.getResourceAsStream("/brigadiertypes.json")) {
                InputStreamReader inputStreamReader = new InputStreamReader(typeStream);
                BrigadierVersion[] versions = new Gson().fromJson(inputStreamReader, BrigadierVersion[].class);
                for (BrigadierVersion version : versions) {
                    for (int v : version.protocolVersions) {
                        versionMap.put(v, version);
                    }
                    for (int i = 0; i < version.argumentTypes.length; i++) {
                        version.typeToInt.put(version.argumentTypes[i], i);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public static ArgumentType<?> getArgumentType(int protocolVersion, String id, Object... arguments) {
        if (arguments == null || arguments.length == 0) {
            switch (id) {
                case "brigadier:bool":
                    return com.mojang.brigadier.arguments.BoolArgumentType.bool();
                case "brigadier:float":
                    return com.mojang.brigadier.arguments.FloatArgumentType.floatArg();
                case "brigadier:double":
                    return com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg();
                case "brigadier:integer":
                    return com.mojang.brigadier.arguments.IntegerArgumentType.integer();
                case "brigadier:long":
                    return com.mojang.brigadier.arguments.LongArgumentType.longArg();
                case "brigadier:string":
                    return com.mojang.brigadier.arguments.StringArgumentType.string();
            }
        }
        if (!id.contains(":")) id = "minecraft:" + id;
        if (protocolVersion < ProtocolConstants.MINECRAFT_1_19) {
            return getLegacyArgumentType(id, arguments);
        }
        BrigadierVersion brigadierVersion = versionMap.get(protocolVersion);
        if (brigadierVersion == null) return null;
        if (arguments == null || arguments.length == 0) {
            ArgumentType<?> type = brigadierVersion.cache.get(id);
            if (type != null) return type;
        }
        ArgumentType<?> type = BungeeBrigadierHack.createArgumentType(protocolVersion, brigadierVersion.typeToInt.get(id), arguments);
        if (type != null && (arguments == null || arguments.length == 0)) {
            brigadierVersion.cache.put(id, type);
        }
        return type;
    }

    private static Map<String, ArgumentType<?>> legacyCache = new ConcurrentHashMap<>();

    private static ArgumentType<?> getLegacyArgumentType(String id, Object... arguments) {
        if (arguments == null || arguments.length == 0) {
            ArgumentType<?> type = legacyCache.get(id);
            if (type != null) return type;
        }
        ArgumentType<?> type = BungeeBrigadierHack.createArgumentType(ProtocolConstants.MINECRAFT_1_18_2, id, arguments);
        if (type != null && (arguments == null || arguments.length == 0)) {
            legacyCache.put(id, type);
        }
        return type;
    }
}
