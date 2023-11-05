package io.siggi.cubecore.nms;

public final class AuthLibProperty {
    private final String name;
    private final String value;
    private final String signature;
    public AuthLibProperty(String name, String value, String signature) {
        this.name = name;
        this.value = value;
        this.signature = signature;
    }
    public String name() {
        return name;
    }
    public String value() {
        return value;
    }
    public String signature() {
        return signature;
    }
}
