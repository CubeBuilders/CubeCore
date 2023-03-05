package io.siggi.cubecore.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class DataAuthentication {
    private DataAuthentication() {
    }

    private static byte[] salt;

    public static void setupSalt(File saltFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(saltFile))) {
            String hex = reader.readLine();
            salt = CubeCoreUtil.hexToBytes(hex);
        } catch (Exception e) {
            SecureRandom random = new SecureRandom();
            salt = new byte[32];
            random.nextBytes(salt);
            try (FileOutputStream out = new FileOutputStream(saltFile)) {
                out.write(CubeCoreUtil.bytesToHex(salt).getBytes());
            } catch (Exception e2) {
            }
        }
    }

    /**
     * Create a hash that you can use to verify the integrity of data later on. The salt used in the hash is unique per
     * server, so a hash created on one server will not be valid on another.
     *
     * @param namespace The namespace to create this hash in
     * @param data      The data you want to ensure does not change
     * @return the hash
     */
    public static String createHash(String namespace, String data) {
        MessageDigest sha256 = CubeCoreUtil.sha256();
        sha256.update((namespace.length() + ":" + namespace + ":").getBytes(StandardCharsets.UTF_8));
        sha256.update(data.getBytes(StandardCharsets.UTF_8));
        sha256.update(salt);
        return CubeCoreUtil.bytesToHex(sha256.digest());
    }

    /**
     * Validate data to ensure to prevent tampering or use of arbitrary values.
     *
     * @param namespace The namespace to verify a hash in
     * @param data      The data to verify
     * @param hash      The hash to verify
     * @return true if everything is good
     */
    public static boolean validateHash(String namespace, String data, String hash) {
        return hash.equals(createHash(namespace, data));
    }
}
