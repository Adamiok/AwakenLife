package me.adamiok.awakenlife.recipes;

import me.adamiok.awakenlife.AwakenLife;
import me.adamiok.awakenlife.items.AwakenItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class AwakenRecipes {
    
    public static void init() {
        FileConfiguration config = AwakenLife.getInstance().getConfig();
        if (config.getBoolean("enable-head-crafting")) {
            headRecipe();
        }
    }
    
    public static void unregister() {
        Bukkit.removeRecipe(new NamespacedKey(AwakenLife.getInstance(), "head"));
    }
    
    private static void headRecipe() {
        ItemStack head = AwakenItems.getPlayerHead();
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(AwakenLife.getInstance(), "head"), head);
        recipe.shape(
                "DTD",
                "TNT",
                "DTD");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
    
        Bukkit.addRecipe(recipe);
    }
}
