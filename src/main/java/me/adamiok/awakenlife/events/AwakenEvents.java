package me.adamiok.awakenlife.events;

import me.adamiok.awakenlife.AwakenLife;
import me.adamiok.awakenlife.data.AwakenData;
import me.adamiok.awakenlife.data.MojangApi;
import me.adamiok.awakenlife.items.AwakenItems;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.UUID;

public class AwakenEvents implements Listener {

    @EventHandler
    public static void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        
        // Check if user is in creative
        if (player.getGameMode() == GameMode.CREATIVE) { return; }

        // Check if killed by player
        String deathMessage = e.getDeathMessage();
        Player killer = null;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (deathMessage != null && !online.getName().equals(player.getName()) && deathMessage.contains(online.getName())) {
                killer = online;
                break;
            }
        }
        if (killer == null) { return; }

        ItemStack skull = AwakenItems.getPlayerHead(player);
        Location location = player.getLocation();

        player.getWorld().dropItemNaturally(location, skull);
        AwakenData.addPlayer(player.getUniqueId(), killer.getName());
        String message = "You have been killed by " + ChatColor.RED + killer.getName() + ChatColor.WHITE + "! Ask another player to place down a player head, renamed to " + ChatColor.AQUA + player.getName();
        Bukkit.getScheduler().scheduleSyncDelayedTask(AwakenLife.getInstance(), ()-> {
            player.spigot().respawn();
            player.kickPlayer(message);
        }, 1);
    }

    @EventHandler
    public static void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();

        // Check if a player head is being placed
        EquipmentSlot hand = e.getHand();
        ItemStack item = player.getInventory().getItem(hand);
        if (item == null) { return; }

        if (!(item.getType() == Material.PLAYER_HEAD)) { return; }
        e.setCancelled(true);

        if (item.getItemMeta() == null) { return; }

        // Get name
        if (!item.getItemMeta().hasDisplayName()) {
            player.sendMessage(ChatColor.RED + "You need to rename the head to the player you want to respawn!");
            return;
        }

        String name = item.getItemMeta().getDisplayName();

        Bukkit.getScheduler().runTaskAsynchronously(AwakenLife.getInstance(), ()-> {
            
            try {
                UUID uuid = MojangApi.playerNameToUuid(name);
                if (AwakenData.isAlive(uuid)) {
                    player.sendMessage(ChatColor.RED + name + " is not dead");
                    return;
                }
                AwakenData.removePlayer(uuid);
                player.sendMessage(ChatColor.GREEN + "Successfully respawned " + name + "! Ask them to join back");
                item.setAmount(item.getAmount() - 1);
            } catch (IOException ex) {
                player.sendMessage(ChatColor.RED + "An connection error has occurred, please try again in a few minutes");
            } catch (InvalidParameterException ex) {
                player.sendMessage(ChatColor.RED + "Player name is not valid");
            }
        });
    }

    @EventHandler
    public static void onPlayerJoin(AsyncPlayerPreLoginEvent e) {
        UUID uuid = e.getUniqueId();
        if (AwakenData.isAlive(uuid)) { return; }
        try {
            String killer = AwakenData.getKiller(uuid);
            String message = "You have been killed by " + ChatColor.RED + killer + ChatColor.WHITE + "! Ask another player to place down a player head, renamed to " + ChatColor.AQUA + e.getName();
            e.setKickMessage(message);
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message);
        } catch (RuntimeException ex) {
            String message = ChatColor.RED + "An error has occurred, please contact the owner and give them this error: \n AWAKEN_DATA_THROWN_IOEXCEPTION";
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
        }
    }

    @EventHandler
    public static void onItemDespawn(ItemDespawnEvent e) {
        if (!AwakenLife.getInstance().getConfig().getBoolean("force-heads-in-inventory")) { return; }
        Item item = e.getEntity();
        item.setUnlimitedLifetime(true);
        e.setCancelled(true);
    }

    @EventHandler
    public static void onItemDrop(PlayerDropItemEvent e) {
        if (!AwakenLife.getInstance().getConfig().getBoolean("force-heads-in-inventory")) { return; }
        Item item = e.getItemDrop();
        if (item.getItemStack().getType() != Material.PLAYER_HEAD) { return; }
        Player player = e.getPlayer();
        e.setCancelled(true);
        player.sendMessage(ChatColor.RED + "You can't drop this item. To give it to someone else use /awaken transfer <PlayerName>");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public static void onItemSpawn(ItemSpawnEvent e) {
        if (!AwakenLife.getInstance().getConfig().getBoolean("force-heads-in-inventory")) { return; }
        Item item = e.getEntity();
        if (item.getItemStack().getType() != Material.PLAYER_HEAD) { return; }
        item.setUnlimitedLifetime(true);
        item.setInvulnerable(true);
    }

    @EventHandler
    public static void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (!AwakenLife.getInstance().getConfig().getBoolean("force-heads-in-inventory")) { return; }
        Entity entity = e.getRightClicked();
        if (!(entity instanceof ItemFrame)) { return; }
        Player player = e.getPlayer();
        EquipmentSlot hand = e.getHand();
        ItemStack itemStack = player.getInventory().getItem(hand);
        if (itemStack == null) { return; }
        if (itemStack.getType() != Material.PLAYER_HEAD) { return; }
        e.setCancelled(true);
        player.sendMessage(ChatColor.RED + "You can't place player heads in item frames");
    }

    @EventHandler
    public static void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if (!AwakenLife.getInstance().getConfig().getBoolean("force-heads-in-inventory")) { return; }
        Entity entity = e.getRightClicked();
        if (!(entity instanceof ArmorStand)) { return; }
        Player player = e.getPlayer();
        EquipmentSlot hand = e.getHand();
        ItemStack itemStack = player.getInventory().getItem(hand);
        if (itemStack == null) { return; }
        if (itemStack.getType() != Material.PLAYER_HEAD) { return; }
        e.setCancelled(true);
        player.sendMessage(ChatColor.RED + "You can't place heads on armor stands");
    }

    @EventHandler
    public static void onInventoryClick(InventoryClickEvent e) {
        if (!AwakenLife.getInstance().getConfig().getBoolean("force-heads-in-inventory")) { return; }
        Player player = (Player) e.getWhoClicked();
        Inventory clicked = e.getClickedInventory();
        if (clicked == null) { return; }
        if (clicked.getType() == InventoryType.ANVIL) { return; }
        
        if (e.getClick() == ClickType.NUMBER_KEY) {
            int slot = e.getHotbarButton();
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null) { return; }
            if (!(item.getType() == Material.PLAYER_HEAD)) { return; }
        } else if (e.isShiftClick()) {
            ItemStack item = e.getCurrentItem();
            if (item == null) { return; }
            if (!(item.getType() == Material.PLAYER_HEAD)) { return; }
            if (!(player.getInventory() == clicked)) { return; }
        } else {
            if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
                return;
            } else if (player.getInventory() == clicked) { return; }
            ItemStack cursor = e.getCursor();
            if (cursor == null) { return; }
            if (!(cursor.getType() == Material.PLAYER_HEAD)) { return; }
        }
        e.setCancelled(true);
    }

    @EventHandler
    public static void onInventoryDrag(InventoryDragEvent e) {
        if (!AwakenLife.getInstance().getConfig().getBoolean("force-heads-in-inventory")) { return; }
        Inventory inventory = e.getInventory();
        if (inventory.getType() == InventoryType.ANVIL) { return; }
        ItemStack cursor = e.getOldCursor();
        if (!(cursor.getType() == Material.PLAYER_HEAD)) { return; }
        for (int slot : e.getRawSlots()) {
            if (slot < inventory.getSize()) {
                e.setCancelled(true);
                break;
            }
        }
    }
}
