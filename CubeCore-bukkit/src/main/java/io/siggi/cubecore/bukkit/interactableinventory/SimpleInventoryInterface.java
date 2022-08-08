package io.siggi.cubecore.bukkit.interactableinventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SimpleInventoryInterface extends InteractableInventory {

    private final ClickHandler clickHandler;
    private final CloseHandler closeHandler;

    public SimpleInventoryInterface(Inventory inventory, ClickHandler clickHandler, CloseHandler closeHandler) {
        super(inventory);
        this.clickHandler = clickHandler;
        this.closeHandler = closeHandler;
    }

    public SimpleInventoryInterface(int rows, ClickHandler clickHandler, CloseHandler closeHandler) {
        super(rows);
        this.clickHandler = clickHandler;
        this.closeHandler = closeHandler;
    }

    public SimpleInventoryInterface(String title, int rows, ClickHandler clickHandler, CloseHandler closeHandler) {
        super(title, rows);
        this.clickHandler = clickHandler;
        this.closeHandler = closeHandler;
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (clickHandler != null) {
            HumanEntity whoClicked = event.getWhoClicked();
            if (!(whoClicked instanceof Player))
                return;
            clickHandler.handle((Player) whoClicked, event, event.getRawSlot(), event.getCurrentItem(), event.getClick());
        }
    }

    public void handleInventoryCreative(InventoryCreativeEvent event) {
        event.setCancelled(true);
    }

    public void handleInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    public void handleInventoryOpen(InventoryOpenEvent event) {
    }

    public void handleInventoryClose(InventoryCloseEvent event) {
        if (closeHandler != null) {
            closeHandler.handle((Player) event.getPlayer(), event);
        }
    }

    @FunctionalInterface
    public interface ClickHandler {
        public void handle(Player p, InventoryClickEvent event, int slot, ItemStack stack, ClickType clickType);
    }

    @FunctionalInterface
    public interface CloseHandler {
        public void handle(Player p, InventoryCloseEvent event);
    }
}