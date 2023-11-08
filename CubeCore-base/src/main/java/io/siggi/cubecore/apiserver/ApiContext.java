package io.siggi.cubecore.apiserver;

import io.siggi.http.HTTPRequest;
import io.siggi.http.HTTPResponse;
import io.siggi.simplejwt.JWTToken;
import io.siggi.simplejwt.alg.JWTAlgorithm;

public final class ApiContext {
    private final HTTPRequest request;
    private final JWTToken authToken;

    ApiContext(HTTPRequest request, JWTAlgorithm jwtAlgorithm) {
        this.request = request;

        long now = System.currentTimeMillis();
        long nowSeconds = now / 1000L;
        JWTToken authToken = null;
        authToken:
        if (jwtAlgorithm != null) {
            String authorization = request.getHeader("Authorization");
            if (authorization == null) break authToken;
            if (!authorization.toLowerCase().startsWith("bearer ")) break authToken;
            JWTToken token;
            try {
                token = JWTToken.parse(authorization.substring(7));
            } catch (Exception e) {
                break authToken;
            }
            if (!token.isValid(nowSeconds)) break authToken;
            if (!jwtAlgorithm.verify(token)) break authToken;
            authToken = token;
        }
        this.authToken = authToken;
    }

    public HTTPRequest getRequest() {
        return request;
    }

    public HTTPResponse getResponse() {
        return request.response;
    }

    public JWTToken getAuthToken() {
        return authToken;
    }
}
