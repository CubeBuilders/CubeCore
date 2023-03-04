package io.siggi.cubecore.bungee;

import io.siggi.cubecore.BrandReceiver;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BrandReceiverBungee extends BrandReceiver implements Listener {

    @EventHandler
    public void pluginMessage(PluginMessageEvent event) {
        if (event.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) event.getSender();
            if (event.getTag().equals("MC|Brand") || event.getTag().equals("minecraft:brand")) {
                receivedBrandPluginMessage(p.getUniqueId(), event.getData());
            }
        }
    }
}
