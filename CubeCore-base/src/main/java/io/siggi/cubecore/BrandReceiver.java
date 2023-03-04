package io.siggi.cubecore;

import io.siggi.cubecore.util.VarInt;
import java.io.ByteArrayInputStream;
import java.util.UUID;

public abstract class BrandReceiver {
    protected void receivedBrandPluginMessage(UUID player, byte[] data) {
        String brand = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            int v = VarInt.read(bais);
            int a = bais.available();
            if (v == a) {
                byte[] bb = new byte[v];
                bais.read(bb, 0, bb.length);
                brand = new String(bb);
            }
        } catch (Exception e) {
        }
        if (brand == null) return;
        setClientBrand(player, brand);
    }

    private void setClientBrand(UUID player, String brand) {

    }
}
