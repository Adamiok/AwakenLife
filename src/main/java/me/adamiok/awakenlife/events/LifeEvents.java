package me.adamiok.awakenlife.events;

import me.adamiok.awakenlife.AwakenLife;
import me.adamiok.awakenlife.data.LifeData;
import me.adamiok.awakenlife.data.MojangApi;
import me.adamiok.awakenlife.items.LifeItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.UUID;

public class LifeEvents implements Listener {
    
    @EventHandler
    public static void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        String deathMessage = e.getDeathMessage();
        
        if (deathMessage == null) { return; }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p != player && deathMessage.contains(p.getName())) {
                return;
            }
        }
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute == null) { return; }
        double health = attribute.getValue();
        if (health <= 2) {
            String message = "You have run out of hearts! Ask another player to place a heart revive stone, renamed to " + ChatColor.AQUA + player.getName();
            Bukkit.getScheduler().scheduleSyncDelayedTask(AwakenLife.getInstance(), ()-> {
                player.spigot().respawn();
                LifeData.addPlayer(player.getUniqueId());
                player.kickPlayer(message);
            }, 1);
            return;
        }
        attribute.setBaseValue(health - 2);
    }
    
    @EventHandler
    public static void onPlayerJoin(AsyncPlayerPreLoginEvent e) {
        UUID uuid = e.getUniqueId();
        if (LifeData.isBanned(uuid)) {
             String message = "You have run out of hearts! Ask another player to place a Heart Revive Stone, renamed to " + ChatColor.AQUA + e.getName();
             e.setKickMessage(message);
             e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message);
        }
    }
    
    @EventHandler
    public static void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        EquipmentSlot hand = e.getHand();
        ItemStack item = player.getInventory().getItem(hand);
        if (item == null) { return; }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) { return; }
        if (meta.getLore() == null) { return; }
        if (meta.getLore().equals(LifeItems.HEART.getItemMeta().getLore())) {
            // heart
            e.setCancelled(true);
            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attribute == null) { return; }
            int health = (int) attribute.getValue();
            int maxHealth = AwakenLife.getInstance().getConfig().getInt("max-hearts") * 2;
            
            if (health >= maxHealth) {
                player.sendMessage(ChatColor.RED + "You can only use this item if you have less than " + maxHealth /2 + " hearts!");
                return;
            }
            player.sendMessage(ChatColor.GREEN + "Added a heart!");
            attribute.setBaseValue(health + 2);
            item.setAmount(item.getAmount() - 1);
        } else if (meta.getLore().equals(LifeItems.HEART_STONE.getItemMeta().getLore())) {
            // heart stone
            e.setCancelled(true);
            if (!meta.hasDisplayName()) {
                player.sendMessage(ChatColor.RED + "Rename to the player you want to respawn!");
                return;
            }
            
            String name = meta.getDisplayName();
            
            Bukkit.getScheduler().runTaskAsynchronously(AwakenLife.getInstance(), ()-> {
                try {
                    UUID uuid = MojangApi.playerNameToUuid(name);
                    
                    if (!LifeData.isBanned(uuid)) {
                        player.sendMessage(ChatColor.RED + "Player is not banned by losing all hearts");
                        return;
                    }
                    LifeData.removePlayer(uuid);
                    player.sendMessage(ChatColor.GREEN + "Player unbanned ask them to join back! But, be aware they will be on 1 heart");
                    item.setAmount(item.getAmount() - 1);
                } catch (IOException ex) {
                    player.sendMessage(ChatColor.RED + "An connection error has occurred, please try again in a few minutes");
                } catch (InvalidParameterException ex) {
                    player.sendMessage(ChatColor.RED + "Player name is not valid");
                }
            });
        }
    }
}