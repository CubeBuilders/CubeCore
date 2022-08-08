package io.siggi.cubecore.usercache;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public final class TextureCache {
    private static final JsonParser jsonParser = new JsonParser();
    private final File rootDirectory;

    public TextureCache(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    private File getFile(UUID uuid) {
        String uuidLower = uuid.toString().toLowerCase().replace("-", "");
        return new File(rootDirectory, uuidLower.substring(0, 2) + "/" + uuidLower.substring(2, 4) + "/" + uuidLower + ".txt");
    }

    public Entry get(UUID uuid) {
        try (BufferedReader reader = new BufferedReader(new FileReader(getFile(uuid)))) {
            String payload = reader.readLine();
            String signature = reader.readLine();
            if (payload == null || signature == null) {
                return null;
            }
            return new Entry(payload, signature);
        } catch (Exception e) {
            return null;
        }
    }

    public void store(UUID uuid, String payload, String signature) {
        if (uuid == null || payload == null || signature == null) throw new NullPointerException();
        File file = getFile(uuid);
        File parent = file.getParentFile();
        if (!parent.exists()) parent.mkdirs();
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write((payload + "\n" + signature + "\n").getBytes());
        } catch (Exception e) {
        }
    }

    public class Entry {
        private final String payload;
        private final String signature;
        private final JsonObject payloadObject;

        private Entry(String payload, String signature) {
            this.payload = payload;
            this.signature = signature;
            JsonObject payloadObj;
            try {
                payloadObj = jsonParser.parse(new String(Base64.getDecoder().decode(payload), StandardCharsets.UTF_8)).getAsJsonObject();
            } catch (Exception e) {
                payloadObj = null;
            }
            payloadObject = payloadObj;
        }

        public String getPayload() {
            return payload;
        }

        public String getSignature() {
            return signature;
        }

        public String getSkin() {
            try {
                return payloadObject.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
            } catch (Exception e) {
                return null;
            }
        }

        public String getModel() {
            try {
                return payloadObject.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("metadata").getAsJsonObject().get("model").getAsString();
            } catch (Exception e) {
                return "classic";
            }
        }

        public String getCape() {
            try {
                return payloadObject.get("textures").getAsJsonObject().get("CAPE").getAsJsonObject().get("url").getAsString();
            } catch (Exception e) {
                return null;
            }
        }
    }
}
