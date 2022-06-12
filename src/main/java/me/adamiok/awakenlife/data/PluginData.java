package me.adamiok.awakenlife.data;

import me.adamiok.awakenlife.AwakenLife;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

import java.io.File;

public class PluginData {
    
    public static void checkConfig() {
        AwakenLife instance = AwakenLife.getInstance();
        
        if (instance.getConfig().getDouble("config-version") == AwakenLife.configVersion) { return; }
        File file = new File(instance.getDataFolder().getPath() + "/config.yml");
        boolean success = file.delete();
        if (!success) {
            instance.getLogger().severe("Can't delete config file, must be deleted manually for the config to update");
        }
        instance.saveDefaultConfig();
        
        instance.getLogger().warning("Using outdated configuration file, resetting");
        instance.getLogger().info("Please review the newly created config file");
        
        instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> {
            instance.getServer().getPluginManager().disablePlugin(instance);
        }, 1);
    }
    
    public static void addConfigChart(Metrics metrics) {
        AwakenLife instance = AwakenLife.getInstance();
        
        metrics.addCustomChart(new SimplePie("enable-awaken", ()-> {
            return String.valueOf(instance.getConfig().getBoolean("enable-awaken"));
        }));
        
        metrics.addCustomChart(new SimplePie("enable-head-crafting", ()-> {
            return String.valueOf(instance.getConfig().getBoolean("enable-head-crafting"));
        }));
        
        metrics.addCustomChart(new SimplePie("allow-transfer-heads", ()-> {
            return String.valueOf(instance.getConfig().getBoolean("allow-transfer-heads"));
        }));
        
        metrics.addCustomChart(new SimplePie("force-heads-in-inventory", ()-> {
            return String.valueOf(instance.getConfig().getBoolean("force-heads-in-inventory"));
        }));
        
        metrics.addCustomChart(new SimplePie("enable-life", ()-> {
            return String.valueOf(instance.getConfig().getBoolean("enable-life"));
        }));
        
        metrics.addCustomChart(new SimplePie("enable-hearts-crafting", ()-> {
            return String.valueOf(instance.getConfig().getBoolean("enable-hearts-crafting"));
        }));
        
        metrics.addCustomChart(new SimplePie("max-hearts", ()-> {
            return String.valueOf(instance.getConfig().getInt("max-hearts"));
        }));
        
        metrics.addCustomChart(new SimplePie("enable-heart-stones-crafting", ()-> {
            return String.valueOf(instance.getConfig().getBoolean("enable-heart-stones-crafting"));
        }));
        
        metrics.addCustomChart(new SimplePie("config-version", ()-> {
            return String.valueOf(instance.getConfig().getDouble("config-version"));
        }));
    }
}
