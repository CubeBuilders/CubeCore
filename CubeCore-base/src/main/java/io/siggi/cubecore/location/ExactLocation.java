package io.siggi.cubecore.location;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Objects;

public final class ExactLocation {
    public static final TypeAdapter<ExactLocation> typeAdapter = new TypeAdapter<ExactLocation>() {
        @Override
        public ExactLocation read(JsonReader reader) throws IOException {
            double x = 0;
            double y = 0;
            double z = 0;
            float pitch = 0;
            float yaw = 0;
            reader.beginObject();
            while (reader.peek() != JsonToken.END_OBJECT) {
                String name = reader.nextName();
                switch (name) {
                    case "x": {
                        x = reader.nextDouble();
                    }
                    break;
                    case "y": {
                        y = reader.nextDouble();
                    }
                    break;
                    case "z": {
                        z = reader.nextDouble();
                    }
                    break;
                    case "pitch": {
                        pitch = (float) reader.nextDouble();
                    }
                    break;
                    case "yaw": {
                        yaw = (float) reader.nextDouble();
                    }
                    break;
                }
            }
            reader.endObject();
            return new ExactLocation(x, y, z, pitch, yaw);
        }

        @Override
        public void write(JsonWriter writer, ExactLocation location) throws IOException {
            writer.beginObject();
            writer.name("x").value(location.x);
            writer.name("y").value(location.y);
            writer.name("z").value(location.z);
            writer.name("pitch").value(location.pitch);
            writer.name("yaw").value(location.yaw);
            writer.endObject();
        }
    };

    private final double x, y, z;
    private final float pitch, yaw;

    public ExactLocation(double x, double y, double z, float pitch, float yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof ExactLocation)) return false;
        ExactLocation o = (ExactLocation) other;
        return x == o.x
                && y == o.y
                && z == o.z
                && pitch == o.pitch
                && yaw == o.yaw;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, pitch, yaw);
    }
}
