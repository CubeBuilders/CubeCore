package io.siggi.cubecore.apiserver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.siggi.cubecore.util.CubeCoreUtil;
import io.siggi.http.HTTPRequest;
import io.siggi.http.HTTPServer;
import io.siggi.http.HTTPServerBuilder;
import io.siggi.http.session.Sessions;
import io.siggi.simplejwt.alg.JWTAlgorithm;
import io.siggi.simplejwt.alg.RS;
import io.siggi.simplersa.RSAKeyManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

public final class ApiServerImpl implements ApiServer {
    private static final Gson gson = new Gson();
    private final JWTAlgorithm jwtAlgorithm;
    private final File sessionsDirectory;
    private HTTPServer httpServer;
    private ServerSocket serverSocket;
    private final String localIp;
    private final int port;
    private final String publicEndpoint;
    private boolean started = false;
    private final Map<String,ApiHandler> handlers = new HashMap<>();
    private final ThreadLocal<ApiContext> apiContextThreadLocal = new ThreadLocal<>();

    public ApiServerImpl(File configFile, File sessionsDirectory) throws ApiServerStartException {
        this(readConfigFile(configFile), sessionsDirectory);
    }

    private static ApiServerConfig readConfigFile(File configFile) throws ApiServerStartException {
        try (FileReader reader = new FileReader(configFile)) {
            return gson.fromJson(reader, ApiServerConfig.class);
        } catch (IOException e) {
            throw new ApiServerStartException(e);
        }
    }

    public ApiServerImpl(ApiServerConfig config, File sessionsDirectory) throws ApiServerStartException {
        try {
            this.sessionsDirectory = sessionsDirectory;
            localIp = config.localIp == null ? "0.0.0.0" : config.localIp;
            port = config.port;
            String publicEndpoint = config.publicEndpoint;
            if (publicEndpoint != null && publicEndpoint.endsWith("/")) publicEndpoint = publicEndpoint.substring(0, publicEndpoint.length()-1);
            this.publicEndpoint = publicEndpoint;
            String jwtAlgorithm = config.jwtAlgorithm;
            if (jwtAlgorithm != null) {
                Class<? extends JWTAlgorithm> algClass = (Class<? extends JWTAlgorithm>) Class.forName("io.siggi.simplejwt.alg." + (jwtAlgorithm.toUpperCase()));
                byte[] jwtKey = CubeCoreUtil.hexToBytes(config.jwtKey);
                if (RS.class.isAssignableFrom(algClass)) {
                    PrivateKey privateKey = null;
                    PublicKey publicKey = RSAKeyManager.loadPublic(jwtKey);
                    String jwtPrivateString = config.jwtPrivateKey;
                    if (jwtPrivateString != null) {
                        byte[] jwtPrivate = CubeCoreUtil.hexToBytes(jwtPrivateString);
                        privateKey = RSAKeyManager.loadPrivate(jwtPrivate);
                    }
                    Constructor<? extends JWTAlgorithm> constructor = algClass.getConstructor(PrivateKey.class, PublicKey.class);
                    this.jwtAlgorithm = constructor.newInstance(privateKey, publicKey);
                } else {
                    Constructor<? extends JWTAlgorithm> constructor = algClass.getConstructor(byte[].class);
                    this.jwtAlgorithm = constructor.newInstance((Object) jwtKey);
                }
            } else {
                this.jwtAlgorithm = null;
            }
            for (ApiServerConfig.MountPoint mountPoint : config.mountPoints) {
                mountPath(mountPoint.mountPoint, new File(mountPoint.path));
            }
        } catch (ReflectiveOperationException | InvalidKeySpecException e) {
            throw new ApiServerStartException(e);
        }
    }

    @Override
    public void start() throws ApiServerStartException {
        if (started) return;
        started = true;

        try {
            InetAddress bindAddress = null;
            if (!localIp.equals("0.0.0.0")) {
                bindAddress = InetAddress.getByName(localIp);
            }
            serverSocket = new ServerSocket(port, 0, bindAddress);
            HTTPServerBuilder httpServerBuilder = new HTTPServerBuilder();
            if (sessionsDirectory != null && !sessionsDirectory.exists()) sessionsDirectory.mkdirs();
            httpServerBuilder.setSessions(Sessions.create(3600000L, sessionsDirectory));
            httpServer = httpServerBuilder.build();
            httpServer.responderRegistry.register("/", this::handleRequest, true, true);
        } catch (IOException e) {
            started = false;
            throw new ApiServerStartException(e);
        }

        new Thread(() -> {
            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    httpServer.handle(socket);
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    @Override
    public void close() {
        try {
            serverSocket.close();
        } catch (Exception ignored) {
        }
    }

    private void handleRequest(HTTPRequest request) throws Exception {
        try {
            ApiContext context = new ApiContext(request, jwtAlgorithm);
            apiContextThreadLocal.set(context);
            String path = request.url;
            ApiHandler handler;
            while (true) {
                handler = handlers.get(path);
                if (handler != null) break;
                path = upPath(path);
                if (path.isEmpty()) break;
            }
            if (handler != null) {
                try {
                    handler.handleRequest(request, context);
                } catch (ApiForbiddenAccessException e) {
                    request.response.setHeader("403 Forbidden");
                    request.response.setHeader("Content-Type", "application/json");
                    JsonObject object = new JsonObject();
                    object.addProperty("error", "Access denied");
                    request.response.write(object.toString());
                }
            }
            if (!request.alreadyWrote()) {
                request.response.setHeader("404 Not Found");
                request.response.setHeader("Content-Type", "application/json");
                JsonObject object = new JsonObject();
                object.addProperty("error", "Not found");
                request.response.write(object.toString());
            }
        } finally {
            apiContextThreadLocal.remove();
        }
    }

    @Override
    public void addHandler(String path, ApiHandler handler) {
        if (path == null || handler == null) throw new NullPointerException();
        if (!path.startsWith("/")) throw new IllegalArgumentException("Path must start with /");
        handlers.put(path, handler);
    }

    @Override
    public void mountPath(String path, File directory) {
        if (path == null || directory == null) throw new NullPointerException();
        String prefix = path.endsWith("/") ? path : (path + "/");
        addHandler(path, (request, context) -> {
            if (!request.url.startsWith(prefix)) return;
            String subPath = request.url.substring(prefix.length());
            if (subPath.contains("/../") || subPath.startsWith("../") || subPath.endsWith("/..")) return;
            File file = new File(directory, subPath);
            if (!file.isFile()) {
                if (file.isDirectory()) {
                    File indexFile = new File(file, "index.html");
                    if (indexFile.isFile()) {
                        if (!request.url.endsWith("/")) {
                            request.response.redirect(request.url + "/");
                            return;
                        }
                        file = indexFile;
                    }
                } else {
                    return;
                }
            }
            request.response.returnFile(file);
        });
    }

    @Override
    public JWTAlgorithm getJWTAlgorithm() {
        return jwtAlgorithm;
    }

    public String getLocalIp() {
        return localIp;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isPubliclyAccessible() {
        return publicEndpoint != null;
    }

    @Override
    public String getPublicEndpoint() {
        return publicEndpoint;
    }

    private static String upPath(String path) {
        int position = path.lastIndexOf("/");
        if (position < 0) return "";
        if (position == path.length() - 1) {
            return path.substring(0, position);
        } else {
            return path.substring(0, position + 1);
        }
    }
}
