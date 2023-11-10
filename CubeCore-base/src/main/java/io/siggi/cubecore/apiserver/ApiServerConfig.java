package io.siggi.cubecore.apiserver;

import java.util.ArrayList;
import java.util.List;

public class ApiServerConfig {
    public String localIp;
    public int port;
    public String publicEndpoint;
    public String jwtAlgorithm;
    public String jwtKey;
    public String jwtPrivateKey;
    public final List<MountPoint> mountPoints = new ArrayList<>();

    public static class MountPoint {
        public String mountPoint;
        public String path;
    }
}
