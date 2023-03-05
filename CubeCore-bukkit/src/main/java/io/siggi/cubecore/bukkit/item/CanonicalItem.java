package io.siggi.cubecore.bukkit.item;

import io.siggi.nbt.NBTCompound;
import io.siggi.nbt.NBTToolBukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class CanonicalItem {
    final Plugin plugin;
    private final String id;
    private final NBTCompound stack;
    private final ItemStack item;

    private boolean automatic = false;
    private int automaticSlot = -1;
    private boolean givenOnCrossWorldTeleport = false;

    CanonicalItem(Plugin plugin, String id, NBTCompound stack) {
        this.plugin = plugin;
        this.id = id;
        this.stack = stack;
        this.item = NBTToolBukkit.itemFromNBT(stack);
    }

    public boolean matches(ItemStack item) {
        if (item == null)
            return false;
        NBTCompound tag = NBTToolBukkit.getTag(item);
        if (tag == null)
            return false;
        String id = tag.getString("canonicalItemId");
        return id != null && id.equals(this.id);
    }

    public boolean matchesExactly(ItemStack item) {
        if (item == null)
            return false;
        return item.isSimilar(this.item);
    }

    public String getId() {
        return id;
    }

    public ItemStack getItemStack() {
        return NBTToolBukkit.itemFromNBT(stack);
    }

    public ItemStack getItemStack(int count) {
        ItemStack itemStack = getItemStack();
        itemStack.setAmount(count);
        return itemStack;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public CanonicalItem setAutomatic(boolean automatic) {
        this.automatic = automatic;
        return this;
    }

    public int getAutomaticSlot() {
        return automaticSlot;
    }

    public CanonicalItem setAutomaticSlot(int automaticSlot) {
        this.automatic = true;
        this.automaticSlot = automaticSlot;
        return this;
    }

    public boolean isGivenOnCrossWorldTeleport() {
        return givenOnCrossWorldTeleport;
    }

    public CanonicalItem setGivenOnCrossWorldTeleport(boolean given) {
        this.givenOnCrossWorldTeleport = given;
        return this;
    }
}
