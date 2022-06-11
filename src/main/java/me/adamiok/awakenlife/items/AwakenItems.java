package me.adamiok.awakenlife.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class AwakenItems {

    public static ItemStack getPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwnerProfile(player.getPlayerProfile());
        head.setItemMeta(meta);

        ItemMeta itemMeta = head.getItemMeta();
        itemMeta.setDisplayName(player.getName());
        List<String> lore = new ArrayList<>();
        lore.add("Rename this item to the player");
        lore.add("you want to respawn.");
        lore.add("Then, place it down!");
        itemMeta.setLore(lore);
        head.setItemMeta(itemMeta);

        return head;
    }
    
    public static ItemStack getPlayerHead() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName("Revive Head");
        List<String> lore = new ArrayList<>();
        lore.add("Rename this item to the player");
        lore.add("you want to respawn.");
        lore.add("Then, place it down!");
        meta.setLore(lore);
        head.setItemMeta(meta);
        
        return head;
    }
}
