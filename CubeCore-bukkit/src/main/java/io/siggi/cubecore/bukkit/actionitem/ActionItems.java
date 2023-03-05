package io.siggi.cubecore.bukkit.actionitem;

import io.siggi.cubecore.bukkit.CubeCoreBukkit;
import io.siggi.cubecore.bukkit.item.CanonicalItems;
import io.siggi.nbt.NBTCompound;
import io.siggi.nbt.NBTToolBukkit;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import static org.bukkit.event.inventory.InventoryAction.DROP_ALL_CURSOR;
import static org.bukkit.event.inventory.InventoryAction.DROP_ALL_SLOT;
import static org.bukkit.event.inventory.InventoryAction.DROP_ONE_CURSOR;
import static org.bukkit.event.inventory.InventoryAction.DROP_ONE_SLOT;
import static org.bukkit.event.inventory.InventoryAction.HOTBAR_SWAP;

public class ActionItems {
    private static final Listener listener = new BukkitActionItemsListener();

    public static Listener getListener() {
        return listener;
    }

    public static String getAction(ItemStack item) {
        NBTCompound tag = NBTToolBukkit.getTag(item);
        if (tag == null) return null;
        String action = tag.getString("actionitem-action");
        if (action == null || action.equals("")) return null;
        return action;
    }

    public static boolean isActionItem(ItemStack item) {
        return getAction(item) != null;
    }

    public static DropBehavior getDropBehavior(ItemStack item) {
        NBTCompound tag = NBTToolBukkit.getTag(item);
        if (tag == null) return DropBehavior.DROP;
        String action = tag.getString("actionitem-action");
        if (action == null || action.equals("")) return DropBehavior.DROP;
        String dropAction = tag.getString("actionitem-drop");
        if (dropAction == null || dropAction.equals("")) return DropBehavior.DESTROY;
        DropBehavior dropBehavior;
        try {
            dropBehavior = DropBehavior.valueOf(dropAction.toUpperCase());
        } catch (IllegalArgumentException iae) {
            dropBehavior = DropBehavior.DESTROY;
        }
        return dropBehavior;
    }

    private static void cleanInventory(Player p) {
        cleanInventory(p.getInventory());
        cleanInventory(p.getEnderChest());
    }

    private static void cleanInventory(Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) continue;
            if (isActionItem(item) && CanonicalItems.getId(item) == null) inventory.clear(i);
        }
    }

    public enum DropBehavior {
        DROP, DESTROY, DENY;
    }

    public static class BukkitActionItemsListener implements Listener {
        @EventHandler
        public void playerJoin(PlayerJoinEvent event) {
            cleanInventory(event.getPlayer());
        }

        @EventHandler
        public void playerQuit(PlayerQuitEvent event) {
            cleanInventory(event.getPlayer());
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void useItem(PlayerInteractEvent event) {
            if (event.useItemInHand() == Event.Result.DENY)
                return;
            Action interactAction = event.getAction();
            if (interactAction != Action.RIGHT_CLICK_BLOCK && interactAction != Action.RIGHT_CLICK_AIR)
                return;
            ItemStack item = event.getItem();
            if (item == null)
                return;
            String action = getAction(item);
            if (action == null)
                return;
            event.setCancelled(true);
            CubeCoreBukkit.chatAsPlayer(event.getPlayer(), action);
        }

        @EventHandler
        public void inventoryClickEvent(InventoryClickEvent event) {
            ItemStack cursorItem = event.getCursor();
            ItemStack slotItem = event.getCurrentItem();
            boolean cursorIsAction = isActionItem(cursorItem);
            boolean slotIsAction = isActionItem(slotItem);
            InventoryAction action = event.getAction();
            int size = event.getView().getTopInventory().getSize();
            int rawSlot = event.getRawSlot();
            if (cursorIsAction && (action == DROP_ALL_CURSOR || action == DROP_ONE_CURSOR)) {
                event.setCancelled(true);
                return;
            }
            if (slotIsAction && (action == DROP_ALL_SLOT || action == DROP_ONE_SLOT)) {
                event.setCancelled(true);
                return;
            }
            if (rawSlot < size) {
                // clicked in top inventory
                if (cursorIsAction) {
                    switch (action) {
                        case PLACE_ALL:
                        case PLACE_ONE:
                        case PLACE_SOME:
                        case SWAP_WITH_CURSOR:
                            event.setCancelled(true);
                            return;
                    }
                }
                if (action == HOTBAR_SWAP) {
                    ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                    if (isActionItem(hotbarItem)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            } else {
                // clicked in bottom inventory
                if (slotIsAction) {
                    switch (action) {
                        case MOVE_TO_OTHER_INVENTORY:
                            event.setCancelled(true);
                            return;
                    }
                }
            }
        }

        @EventHandler
        public void inventoryDragEvent(InventoryDragEvent event) {
            int size = event.getView().getTopInventory().getSize();
            for (Map.Entry<Integer, ItemStack> addedItem : event.getNewItems().entrySet()) {
                int slot = addedItem.getKey();
                ItemStack item = addedItem.getValue();
                if (slot < size && isActionItem(item)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        @EventHandler
        public void craftItem(CraftItemEvent event) {
            CraftingInventory inventory = event.getInventory();
            for (ItemStack item : inventory.getMatrix()) {
                if (item == null)
                    continue;
                if (isActionItem(item)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        @EventHandler
        public void dropItem(PlayerDropItemEvent event) {
            ItemStack item = event.getItemDrop().getItemStack();
            switch (getDropBehavior(item)) {
                case DESTROY: {
                    event.getItemDrop().remove();
                }
                break;
                case DENY: {
                    event.setCancelled(true);
                }
                break;
            }
        }

        @EventHandler
        public void onDeath(PlayerDeathEvent event) {
            event.getDrops().removeIf(item -> getDropBehavior(item) != DropBehavior.DROP);
        }
    }
}
