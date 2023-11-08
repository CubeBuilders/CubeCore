package io.siggi.cubecore.apiserver;

import io.siggi.http.HTTPRequest;

@FunctionalInterface
public interface ApiHandler {
    void handleRequest(HTTPRequest request, ApiContext context) throws Exception;
}
