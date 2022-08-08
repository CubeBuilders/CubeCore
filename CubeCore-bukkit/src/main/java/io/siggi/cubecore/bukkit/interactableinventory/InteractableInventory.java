package io.siggi.cubecore.bukkit.interactableinventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

/**
 * {@link InteractableInventory} simplifies the process of creating inventory interfaces by listening for the events and
 * routing the events to the {@link InteractableInventory} object which is intended to be subclassed. An implementation
 * {@link SimpleInventoryInterface} already exists and probably already suits your needs.
 */
public abstract class InteractableInventory {
    private final Inventory inventory;

    /**
     * Create an {@link InteractableInventory} using an existing {@link Inventory}, which <b>must not</b> be an
     * inventory from an entity whether it's a player, mob, or block such as chests, furnaces, etc.
     *
     * @param inventory The inventory to use.
     */
    public InteractableInventory(Inventory inventory) {
        if (inventory == null)
            throw new NullPointerException();
        this.inventory = inventory;
    }

    /**
     * Create an {@link InteractableInventory} with the specified number of rows.
     *
     * @param rows the number of rows the inventory will have
     */
    public InteractableInventory(int rows) {
        this(Bukkit.createInventory(null, ensureValidRows(rows) * 9));
    }

    /**
     * Create an {@link InteractableInventory} with the specified title and number of rows.
     *
     * @param title the title to use for the inventory
     * @param rows  the number of rows the inventory will have
     */
    public InteractableInventory(String title, int rows) {
        this(Bukkit.createInventory(null, ensureValidRows(rows) * 9, title));
    }

    private static int ensureValidRows(int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be between 1 and 6!");
        }
        return rows;
    }

    /**
     * Get the {@link Inventory} that will be used in this interaction so you can add items to it. To show the
     * inventory to the player, you must use {@link InteractableInventory#open(Player)} and <b>not</b>
     * {@link Player#openInventory(Inventory)}.
     *
     * @return the inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Open the inventory interface for the specified {@link Player}.
     *
     * @param p the {@link Player} to open the inventory interface for.
     * @return the {@link InventoryView} for this inventory.
     */
    public InventoryView open(Player p) {
        InventoryView view = p.openInventory(inventory);
        if (view != null) {
            InteractableInventoryListener.instance.menus.put(view.getTopInventory(), this);
        }
        return view;
    }

    /**
     * Subclasses can override this method to handle when a player clicks on a slot in the inventory.
     *
     * @param event the event object
     */
    public void handleInventoryClick(InventoryClickEvent event) {
    }

    /**
     * Subclasses can override this method to handle when a player performs an action while in creative mode.
     *
     * @param event the event object
     */
    public void handleInventoryCreative(InventoryCreativeEvent event) {
    }

    /**
     * Subclasses can override this method to handle when a player drags across the inventory.
     *
     * @param event the event object
     */
    public void handleInventoryDrag(InventoryDragEvent event) {
    }

    /**
     * Subclasses can override this method to handle when a player opens the inventory.
     *
     * @param event the event object
     */
    public void handleInventoryOpen(InventoryOpenEvent event) {
    }

    /**
     * Subclasses can override this method to handle when a player closes the inventory.
     *
     * @param event the event object
     */
    public void handleInventoryClose(InventoryCloseEvent event) {
    }
}
