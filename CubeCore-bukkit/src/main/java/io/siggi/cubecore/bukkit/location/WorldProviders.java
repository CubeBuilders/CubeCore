package io.siggi.cubecore.bukkit.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import static io.siggi.cubecore.util.CubeCoreUtil.iterable;
import static io.siggi.cubecore.util.CubeCoreUtil.iterateCollectionOfCollection;

public class WorldProviders {
    private static final Map<Plugin, List<WorldProvider>> providersByPlugin = new HashMap<>();
    private static final Listener listener = new WorldProvidersListener();

    private WorldProviders() {
    }

    public static void register(Plugin plugin, WorldProvider provider) {
        if (plugin == null || provider == null)
            throw new NullPointerException();
        if (!plugin.isEnabled())
            throw new IllegalStateException("Plugin " + plugin.getName() + " is not enabled!");
        List<WorldProvider> providers = providersByPlugin.computeIfAbsent(plugin, k -> new ArrayList<>());
        providers.add(provider);
    }

    public static boolean isWorldLoadable(UUID uuid, String name) {
        if (Bukkit.getWorld(uuid) != null || Bukkit.getWorld(name) != null) return true;
        for (WorldProvider provider : iterable(iterateCollectionOfCollection(providersByPlugin.values()))) {
            try {
                if (provider.isWorldLoadable(uuid, name))
                    return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static World loadWorld(UUID uuid, String name) {
        for (WorldProvider provider : iterable(iterateCollectionOfCollection(providersByPlugin.values()))) {
            try {
                World world = provider.loadWorld(uuid, name);
                if (world != null)
                    return world;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Listener getListener() {
        return listener;
    }

    private static class WorldProvidersListener implements Listener {
        @EventHandler
        public void onPluginDisable(PluginDisableEvent event) {
            providersByPlugin.remove(event.getPlugin());
        }
    }
}
