package io.siggi.cubecore.pluginmessage;

import java.io.DataInputStream;
import java.util.HashMap;
import java.util.Map;

public class InboundPluginMessageRouter<P> {
    private final Map<String, InboundPluginMessageHandler<P>> mappings = new HashMap<>();

    public InboundPluginMessageRouter() {
    }

    public void setHandler(String subChannel, InboundPluginMessageHandler<P> handler) {
        if (handler == null) {
            mappings.remove(subChannel);
        } else {
            mappings.put(subChannel, handler);
        }
    }

    public void handle(P player, String subChannel, DataInputStream in) {
        InboundPluginMessageHandler<P> handler = mappings.get(subChannel);
        if (handler == null)
            return;
        try {
            handler.handle(player, subChannel, in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
