package io.siggi.cubecore.pluginmessage;

import java.io.DataInputStream;
import java.io.IOException;

@FunctionalInterface
public interface InboundPluginMessageHandler<P> {
    public void handle(P player, String subChannel, DataInputStream in) throws IOException;
}
