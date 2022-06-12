package me.adamiok.awakenlife.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LifeItems {
    
    public static ItemStack HEART;
    public static ItemStack HEART_STONE;

    public static void init() {
        HEART = heart();
        HEART_STONE = heartStone();
    }
    
    private static ItemStack heart() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_PURPLE + "Adds you a heart");
        lore.add(ChatColor.DARK_PURPLE + "Right click on a block to apply");
        meta.setLore(lore);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Heart");
        item.setItemMeta(meta);
        
        return item;
    }
    
    private static ItemStack heartStone() {
        ItemStack item = new ItemStack(Material.STRUCTURE_VOID);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_PURPLE + "Respawns a player who is out of hearts");
        lore.add(ChatColor.DARK_PURPLE + "DOES NOT WORK IF THE PLAYER IS KILLED BY A PLAYER");
        meta.setLore(lore);
        meta.setDisplayName(ChatColor.GOLD + "Heart Revive Stone");
        item.setItemMeta(meta);
        
        return item;
    }
}
