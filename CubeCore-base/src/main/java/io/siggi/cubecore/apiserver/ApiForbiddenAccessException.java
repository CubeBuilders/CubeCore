package io.siggi.cubecore.apiserver;

public class ApiForbiddenAccessException extends Exception {
    public ApiForbiddenAccessException() {
        super();
    }

    public ApiForbiddenAccessException(String message) {
        super(message);
    }

    public ApiForbiddenAccessException(Throwable cause) {
        super(cause);
    }

    public ApiForbiddenAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
