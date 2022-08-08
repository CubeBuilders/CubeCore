package io.siggi.cubecore.pluginmessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class OutboundPluginMessageBuilder {
    private final String subChannel;
    private final ByteArrayOutputStream outputStream;
    private final DataOutputStream dataOutputStream;

    public OutboundPluginMessageBuilder(String subChannel) {
        this.subChannel = subChannel;
        try {
            outputStream = new ByteArrayOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeUTF("CubeCore");
            dataOutputStream.writeUTF(subChannel);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public byte[] getBytes() {
        return outputStream.toByteArray();
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public OutboundPluginMessageBuilder write(DataWriter writer) {
        try {
            writer.write(dataOutputStream);
        } catch (IOException ioe) {
            throw new RuntimeException();
        }
        return this;
    }

    @FunctionalInterface
    public interface DataWriter {
        public void write(DataOutputStream out) throws IOException;
    }
}
