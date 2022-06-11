package me.adamiok.awakenlife.recipes;

import me.adamiok.awakenlife.AwakenLife;
import me.adamiok.awakenlife.items.LifeItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

public class LifeRecipes {
    
    public static void init() {
        FileConfiguration config = AwakenLife.getInstance().getConfig();
        if (config.getBoolean("enable-hearts-crafting")) {
            heartRecipe();
        }
        if (config.getBoolean("enable-heart-stones-crafting")) {
            heartStoneRecipe();
        }
    }
    
    public static void unregister() {
        // Avoid errors on /reload, even if it is unsupported
        Bukkit.removeRecipe(new NamespacedKey(AwakenLife.getInstance(), "heart"));
        Bukkit.removeRecipe(new NamespacedKey(AwakenLife.getInstance(), "heartStone"));
    }
    
    private static void heartRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(AwakenLife.getInstance(), "heart"), LifeItems.HEART);
        recipe.shape(
                "D D",
                "NTN",
                "D D");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        recipe.setIngredient('N', Material.NETHERITE_SCRAP);
    
        Bukkit.addRecipe(recipe);
    }
    
    private static void heartStoneRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(AwakenLife.getInstance(), "heartStone"), LifeItems.HEART_STONE);
        recipe.shape(
                "DHD",
                "SBS",
                "DHD");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('S', Material.STONE);
        recipe.setIngredient('B', Material.BEACON);
        recipe.setIngredient('H', new RecipeChoice.ExactChoice(LifeItems.HEART));
        
        Bukkit.addRecipe(recipe);
    }
}
