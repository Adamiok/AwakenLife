package me.adamiok.awakenlife;

import me.adamiok.awakenlife.commands.AwakenCommands;
import me.adamiok.awakenlife.commands.LifeCommands;
import me.adamiok.awakenlife.data.AwakenData;
import me.adamiok.awakenlife.data.LifeData;
import me.adamiok.awakenlife.data.PluginData;
import me.adamiok.awakenlife.events.AwakenEvents;
import me.adamiok.awakenlife.events.LifeEvents;
import me.adamiok.awakenlife.items.LifeItems;
import me.adamiok.awakenlife.recipes.AwakenRecipes;
import me.adamiok.awakenlife.recipes.LifeRecipes;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class AwakenLife extends JavaPlugin {

    public static final double configVersion = 1.0;
    private static AwakenLife instance;

    @Override
    public void onEnable() {
        instance = this;
        int pluginId = 15437;
        
        getConfig().options().copyDefaults();
        saveDefaultConfig();
    
        Metrics metrics = new Metrics(this, pluginId);
        PluginData.addConfigChart(metrics);
        
        PluginData.checkConfig();
        
        if (getConfig().getBoolean("enable-awaken")) {
            AwakenData.createData();
            AwakenRecipes.init();
            getServer().getPluginManager().registerEvents(new AwakenEvents(), this);
            getCommand("awaken").setExecutor(new AwakenCommands());
        }
        
        if (getConfig().getBoolean("enable-life")) {
            LifeData.createData();
            LifeItems.init();
            LifeRecipes.init();
            getServer().getPluginManager().registerEvents(new LifeEvents(), this);
            getCommand("life").setExecutor(new LifeCommands());
        }
        
        getLogger().info("Plugin has started... Players will lose hearts!");
    }

    @Override
    public void onDisable() {
        AwakenRecipes.unregister();
        LifeRecipes.unregister();
        getLogger().info("Plugin has shutdown... No more heads will be dropped!");
    }

    public static AwakenLife getInstance() { return instance; }
}
