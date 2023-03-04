package io.siggi.cubecore.bukkit;

import io.siggi.cubecore.BrandReceiver;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class BrandReceiverBukkit extends BrandReceiver implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player p, byte[] data) {
        if (channel.equals("MC|Brand") || channel.equals("minecraft:brand")) {
            receivedBrandPluginMessage(p.getUniqueId(), data);
        }
    }
}
