package io.siggi.cubecore.bukkit.commands;

import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

public class CommandUnsignBook implements CommandExecutor, TabCompleter {
    private static final Material UNSIGNED_BOOK;

    static {
        Material unsignedBook;
        try {
            unsignedBook = Material.WRITABLE_BOOK;
        } catch (Throwable t) {
            // pre-1.13 flattening
            unsignedBook = Material.valueOf("BOOK_AND_QUILL");
        }
        UNSIGNED_BOOK = unsignedBook;
    }

    @Override
    // get/setItemInHand may be deprecated
    // but it behaves the same as get/setItemInMainHand
    // and get/setItemInMainHand does not exist in pre-1.9 servers.
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to unsign a book!");
            return true;
        }
        Player p = (Player) sender;
        if (!p.hasPermission("cubecore.unsign")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        PlayerInventory inventory = p.getInventory();

        ItemStack book = inventory.getItemInHand();
        if (book.getType() != Material.WRITTEN_BOOK) {
            if (book.getType() == UNSIGNED_BOOK) {
                sender.sendMessage(ChatColor.RED + "You need to be holding a written book!");
            } else {
                sender.sendMessage(ChatColor.RED + "The book you're holding is already unsigned!");
            }
            return true;
        }

        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        assert bookMeta != null;

        ItemStack newBook = new ItemStack(UNSIGNED_BOOK, 1);
        BookMeta newBookMeta = (BookMeta) newBook.getItemMeta();
        assert newBookMeta != null;

        newBookMeta.setPages(bookMeta.getPages());
        newBook.setItemMeta(newBookMeta);

        inventory.setItemInHand(newBook);

        sender.sendMessage(ChatColor.GREEN + "Your book has been unsigned!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
