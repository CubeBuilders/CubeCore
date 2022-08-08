package io.siggi.cubecore.bukkit.interactableinventory;

import io.siggi.cubecore.bukkit.CubeCoreBukkit;
import java.util.Map;
import java.util.WeakHashMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

/**
 * If you are not a CubeCore developer, you can probably ignore this. :D
 */
public class InteractableInventoryListener implements Listener {
    static InteractableInventoryListener instance = null;
    final Map<Inventory, InteractableInventory> menus = new WeakHashMap<>();
    private final CubeCoreBukkit plugin;

    public InteractableInventoryListener(CubeCoreBukkit plugin) {
        if (plugin == null)
            throw new NullPointerException();
        if (instance != null)
            throw new IllegalStateException("InteractableInventoryListener already created!");
        this.plugin = plugin;
        instance = this;
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        InteractableInventory menu = menus.get(event.getView().getTopInventory());
        if (menu == null)
            return;
        menu.handleInventoryClick(event);
    }

    @EventHandler
    public void handleInventoryCreative(InventoryCreativeEvent event) {
        InteractableInventory menu = menus.get(event.getView().getTopInventory());
        if (menu == null)
            return;
        menu.handleInventoryCreative(event);
    }

    @EventHandler
    public void handleInventoryDrag(InventoryDragEvent event) {
        InteractableInventory menu = menus.get(event.getView().getTopInventory());
        if (menu == null)
            return;
        menu.handleInventoryDrag(event);
    }

    @EventHandler
    public void handleInventoryOpen(InventoryOpenEvent event) {
        InteractableInventory menu = menus.get(event.getView().getTopInventory());
        if (menu == null)
            return;
        menu.handleInventoryOpen(event);
    }

    @EventHandler
    public void handleInventoryClose(InventoryCloseEvent event) {
        InteractableInventory menu = menus.get(event.getView().getTopInventory());
        if (menu == null)
            return;
        menu.handleInventoryClose(event);
    }
}
