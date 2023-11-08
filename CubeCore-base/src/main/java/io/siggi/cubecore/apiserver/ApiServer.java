package io.siggi.cubecore.apiserver;

import io.siggi.simplejwt.alg.JWTAlgorithm;

public interface ApiServer {
    void start() throws ApiServerStartException;

    void close();

    /**
     * Add a handler for requests for this API server, which will handle requests for the specified path and subdirectories.
     *
     * @param path    the path to handle requests for
     * @param handler the handler for the path and its subdirectories
     */
    void addHandler(String path, ApiHandler handler);

    /**
     * Get the JWT algorithm and key used by this API server.
     *
     * @return the JWT algorithm and key
     */
    JWTAlgorithm getJWTAlgorithm();

    /**
     * Get the local port that the server is listening on.
     *
     * @return the port number
     */
    int getPort();

    /**
     * Determine if this API server is accessible on the public internet.
     *
     * @return true if this API server is accessible on the public internet.
     */
    boolean isPubliclyAccessible();

    /**
     * Get the endpoint this API server is accessible at on the public internet if it is accessible, without the trailing slash.
     *
     * @return the endpoint for this API server on the public internet
     */
    String getPublicEndpoint();
}
