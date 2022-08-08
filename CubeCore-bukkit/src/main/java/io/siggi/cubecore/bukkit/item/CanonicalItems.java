package io.siggi.cubecore.bukkit.item;

import io.siggi.cubecore.bukkit.CubeCoreBukkit;
import io.siggi.nbt.NBTCompound;
import io.siggi.nbt.NBTToolBukkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CanonicalItems {
    private static final Map<String, CanonicalItem> items = new HashMap<>();
    private static final Map<Plugin, Set<CanonicalItem>> itemsByPlugin = new HashMap<>();
    private static final List<Listener> listeners;

    static {
        List<Listener> l = new ArrayList<>();
        l.add(new CanonicalItemsListener());
        pickup:
        {
            try {
                Class<?> clazz = Class.forName("org.bukkit.event.entity.EntityPickupItemEvent");
                l.add(new EntityItemPickupListener());
                break pickup;
            } catch (Throwable t) {
            }
            l.add(new PlayerItemPickupListener());
        }
        openContainer:
        {
            try {
                Class<?> clazz = Class.forName("org.bukkit.inventory.BlockInventoryHolder");
                l.add(new OpenContainerListener());
                break openContainer;
            } catch (Throwable t) {
            }
            l.add(new CloseInventoryListener());
        }
        listeners = Collections.unmodifiableList(l);
    }

    private CanonicalItems() {
    }

    public static CanonicalItem create(Plugin plugin, String id, ItemStack stack) {
        if (plugin == null || id == null || stack == null)
            throw new NullPointerException();
        if (!plugin.isEnabled())
            throw new IllegalStateException("Plugin " + plugin.getName() + " is not enabled!");
        NBTCompound tag = NBTToolBukkit.getTag(stack);
        if (tag == null) tag = new NBTCompound();
        tag.setString("canonicalItemId", id);
        stack = NBTToolBukkit.setTag(stack, tag);
        if (stack.getAmount() != 1) {
            stack.setAmount(1);
        }
        NBTCompound nbt = NBTToolBukkit.itemToNBT(stack);
        CanonicalItem canonicalItem = new CanonicalItem(plugin, id, nbt);
        CanonicalItem replacedItem = items.put(id, canonicalItem);
        if (replacedItem != null) {
            Set<CanonicalItem> byPlugin = itemsByPlugin.get(replacedItem.plugin);
            if (byPlugin != null) {
                byPlugin.remove(replacedItem);
                if (byPlugin.isEmpty()) {
                    itemsByPlugin.remove(replacedItem.plugin);
                }
            }
        }
        Set<CanonicalItem> byPlugin = itemsByPlugin.computeIfAbsent(plugin, k -> new HashSet<>());
        byPlugin.add(canonicalItem);
        return canonicalItem;
    }

    public static CanonicalItem get(String id) {
        return items.get(id);
    }

    public static String getId(ItemStack item) {
        NBTCompound tag = NBTToolBukkit.getTag(item);
        if (tag == null)
            return null;
        String id = tag.getString("canonicalItemId");
        if (id == null || id.isEmpty())
            return null;
        return id;
    }

    public static ItemStack replaceItem(ItemStack item) {
        if (item == null)
            return null;
        String id = getId(item);
        if (id == null)
            return item;
        CanonicalItem canonicalItem = get(id);
        if (canonicalItem == null)
            return null;
        if (canonicalItem.matchesExactly(item))
            return item;
        ItemStack newItem = canonicalItem.getItemStack();
        newItem.setAmount(item.getAmount());
        return newItem;
    }

    public static void replaceItems(Inventory inventory) {
        int size = inventory.getSize();
        for (int i = 0; i < size; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null)
                continue;
            String id = getId(item);
            if (id == null)
                continue;
            CanonicalItem canonicalItem = get(id);
            if (canonicalItem == null) {
                inventory.clear(i);
                continue;
            }
            if (canonicalItem.matchesExactly(item))
                continue;
            ItemStack newItem = canonicalItem.getItemStack();
            newItem.setAmount(item.getAmount());
            inventory.setItem(i, newItem);
        }
    }

    public static List<Listener> getListeners() {
        return listeners;
    }

    public static class CanonicalItemsListener implements Listener {
        private CanonicalItemsListener() {
        }

        private void replaceItemsOnPlayer(Player p) {
            replaceItems(p.getInventory());
            replaceItems(p.getEnderChest());
        }

        private void replaceItemsOnPlayerDelayed(Player p) {
            // do it on the next tick just in case another plugin modifies the player's inventory
            // as a result of an event.
            (new BukkitRunnable() {
                @Override
                public void run() {
                    replaceItemsOnPlayer(p);
                }
            }).runTaskLater(CubeCoreBukkit.getInstance(), 1L);
        }

        private void replaceItemsDelayed(Inventory inventory) {
            // do it on the next tick just in case another plugin modifies the player's inventory
            // as a result of an event.
            (new BukkitRunnable() {
                @Override
                public void run() {
                    replaceItems(inventory);
                }
            }).runTaskLater(CubeCoreBukkit.getInstance(), 1L);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void playerJoin(PlayerJoinEvent event) {
            Player p = event.getPlayer();
            replaceItemsOnPlayerDelayed(p);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void playerQuit(PlayerQuitEvent event) {
            Player p = event.getPlayer();
            replaceItemsOnPlayer(p);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void playerTeleport(PlayerTeleportEvent event) {
            Player p = event.getPlayer();
            replaceItemsOnPlayerDelayed(p);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void itemHop(InventoryMoveItemEvent event) {
            ItemStack item = event.getItem();
            ItemStack replacementItem = replaceItem(item);
            if (item == replacementItem)
                return;
            replaceItemsDelayed(event.getSource());
            replaceItemsDelayed(event.getDestination());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPluginDisable(PluginDisableEvent event) {
            Plugin plugin = event.getPlugin();
            items.values().removeIf((item) -> item.plugin == plugin);
            itemsByPlugin.remove(plugin);
        }
    }

    public static class OpenContainerListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void playerOpenContainer(PlayerInteractEvent event) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock == null)
                return;
            BlockState state = clickedBlock.getState();
            if (state instanceof BlockInventoryHolder) {
                BlockInventoryHolder container = (BlockInventoryHolder) state;
                replaceItems(container.getInventory());
            }
        }
    }

    public static class CloseInventoryListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void playerCloseInventory(InventoryCloseEvent event) {
            replaceItems(event.getPlayer().getInventory());
        }
    }

    public static class EntityItemPickupListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void pickupItem(EntityPickupItemEvent event) {
            LivingEntity entity = event.getEntity();
            if (!(entity instanceof Player))
                return;
            Item item = event.getItem();
            ItemStack itemStack = item.getItemStack();
            ItemStack replacementItem = replaceItem(itemStack);
            if (replacementItem == itemStack)
                return;
            if (replacementItem == null) {
                event.setCancelled(true);
                item.remove();
                return;
            }
            item.setItemStack(replacementItem);
            // this will cancel the pickup once and allow it on the next tick
            // otherwise the player gets the wrong item
            event.setCancelled(true);
        }
    }

    public static class PlayerItemPickupListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void pickupItem(PlayerPickupItemEvent event) {
            Item item = event.getItem();
            ItemStack itemStack = item.getItemStack();
            ItemStack replacementItem = replaceItem(itemStack);
            if (replacementItem == itemStack)
                return;
            if (replacementItem == null) {
                event.setCancelled(true);
                item.remove();
                return;
            }
            item.setItemStack(replacementItem);
            // this will cancel the pickup once and allow it on the next tick
            // otherwise the player gets the wrong item
            event.setCancelled(true);
        }
    }
}
