package me.adamiok.awakenlife.commands;

import me.adamiok.awakenlife.AwakenLife;
import me.adamiok.awakenlife.data.AwakenData;
import me.adamiok.awakenlife.data.MojangApi;
import me.adamiok.awakenlife.items.AwakenItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AwakenCommands implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //transfer, revive, kill, head, help
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Received too little arguments");
            return true;
        }
        
        // transfer
        if (args[0].equalsIgnoreCase("transfer")) {
            if (!isPlayer(sender)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command");
                sender.sendMessage(ChatColor.RED + "/awaken transfer <playerName>");
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "Mention a player to transfer item to");
                sender.sendMessage(ChatColor.RED + "/awaken transfer <playerName>");
                return true;
            }
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Received too many arguments");
                sender.sendMessage(ChatColor.RED + "/awaken transfer <playerName>");
                return true;
            }
            if (!AwakenLife.getInstance().getConfig().getBoolean("allow-transfer-heads")) {
                sender.sendMessage(ChatColor.RED + "Head sending is disabled");
                return true;
            }
            
            String targetName = args[1];
            Player target = sender.getServer().getPlayer(targetName);
            Player player = (Player) sender;
            if (target == null) {
                sender.sendMessage(ChatColor.RED + targetName + " not found");
                return true;
            }
            if (!(player.getInventory().getItemInMainHand().getType() == Material.PLAYER_HEAD)) {
                sender.sendMessage(ChatColor.RED + "You need to be holding a player head in your main hand");
                return true;
            }
            if (player == target) {
                sender.sendMessage(ChatColor.RED + "You can't sent a player head to yourself");
                return true;
            }
            
            Location location = player.getLocation();
            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();
            
            Location targetLocation = target.getLocation();
            double dX = Math.abs(targetLocation.getX() - x);
            double dY = Math.abs(targetLocation.getY() - y);
            double dZ = Math.abs(targetLocation.getZ() - z);
            String message = ChatColor.RED + "You need to be at maximum 3 blocks apart from the player and they must be on the same y level.";
            
            if (dX > 3) {
                player.sendMessage(message);
                return true;
            }
            if (dY != 0) {
                player.sendMessage(message);
                return true;
            }
            if (dZ > 3) {
                player.sendMessage(message);
                return true;
            }
    
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemStack air = new ItemStack(Material.AIR);
    
            HashMap<Integer, ItemStack> map = target.getInventory().addItem(item);
            if (!map.isEmpty()) {
                player.sendMessage(ChatColor.RED + targetName + " does not have space in his/her inventory");
                return true;
            }
            
            player.getInventory().setItemInMainHand(air);
            player.sendMessage(ChatColor.GREEN + "Successfully transferred player head to " + targetName);
            
        } else if (args[0].equalsIgnoreCase("revive")) {
            // Revive
            if (!hasPerm(sender, "revive")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command");
                return true;
            }
            if (!checkArgs(args, sender)) { return true; }
            
            String name = args[1];
    
            Bukkit.getScheduler().runTaskAsynchronously(AwakenLife.getInstance(), ()-> {
                try {
                    UUID uuid = MojangApi.playerNameToUuid(name);
                    if (AwakenData.isAlive(uuid)) {
                        sender.sendMessage(ChatColor.RED + name + " is not dead");
                        return;
                    }
                    AwakenData.removePlayer(uuid);
                    sender.sendMessage(ChatColor.GREEN + "Revived " + name);
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + "A connection error has occurred while connecting the Mojang servers, try again in a few minutes");
                } catch (InvalidParameterException e) {
                    sender.sendMessage(ChatColor.RED + name + " name is not valid");
                }
            });
            
        } else if (args[0].equalsIgnoreCase("kill")) {
            // kill
            if (!hasPerm(sender, "kill")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command");
                return true;
            }
            if (!checkArgs(args, sender)) { return true; }
            
            String name = args[1];
            Player kill = Bukkit.getPlayer(name);
            if (kill == null) {
                sender.sendMessage(ChatColor.RED + name + " must be online");
                return true;
            }
            
            ItemStack head = AwakenItems.getPlayerHead(kill);
            Location location = kill.getLocation();
            
            kill.getWorld().dropItemNaturally(location, head);
            AwakenData.addPlayer(kill.getUniqueId(), "[An admin using commands]");
            String message = "You have been killed by " + ChatColor.RED + "[An admin using commands]" + ChatColor.WHITE + "! Ask another player to place down a player head, renamed to " + ChatColor.AQUA + kill.getName();
            Bukkit.broadcastMessage(kill.getName() + " was killed by [An admin using commands]");
            ItemStack[] items = kill.getInventory().getContents();
            for (ItemStack item : items) {
                if (item == null) { continue; }
                kill.getWorld().dropItemNaturally(location, item);
            }
            kill.kickPlayer(message);
            sender.sendMessage(ChatColor.GREEN + "Successfully killed " + kill.getName());
            
        } else if (args[0].equalsIgnoreCase("head")) {
            // head
            if (!isPlayer(sender)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command");
                return true;
            }
            if (!hasPerm(sender, "head")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command");
                return true;
            }
            if (!checkArgs(args, sender)) { return true; }
            
            String name = args[1];
            Player player = (Player) sender;
            
            Player target = Bukkit.getPlayer(name);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + name + " must be online");
                return true;
            }
            
            ItemStack head = AwakenItems.getPlayerHead(target);
            player.getInventory().addItem(head);
            
        } else if (args[0].equalsIgnoreCase("help")) {
            // help
            sender.sendMessage(ChatColor.GOLD + "### Awaken-Life Awaken help ###");
            sender.sendMessage(ChatColor.AQUA + "/awaken help" + ChatColor.WHITE + " : Displays this message");
            sender.sendMessage(ChatColor.AQUA + "/awaken transfer <playerName>" + ChatColor.WHITE + " : Transfer the player head you are holding to the player mentioned");
            
            if (hasPerm(sender, "revive") || hasPerm(sender, "kill") || hasPerm(sender, "head")) {
                sender.sendMessage(ChatColor.GOLD + "## Admin Commands ##");
            } else {
                return true;
            }
            if (hasPerm(sender, "revive")) {
                sender.sendMessage(ChatColor.AQUA + "/awaken revive <playerName>" + ChatColor.WHITE + " : Revives an eliminated player, without the need for a player head");
            }
            if (hasPerm(sender, "kill")) {
                sender.sendMessage(ChatColor.AQUA + "/awaken kill <playerName>" + ChatColor.WHITE + " : Kills the player and drops his player head. The player will also be kicked like normal");
            }
            if (hasPerm(sender, "head")) {
                sender.sendMessage(ChatColor.AQUA + "/awaken head <playerName>" + ChatColor.WHITE + " : Puts the head of the player in your inventory");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown option " + args[0] + "  /awaken help");
        }
        return true;
    }
    
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> arguments = new ArrayList<>();
        
        if (args.length == 1) {
            if (hasPerm(sender, "head") && sender instanceof Player && "head".startsWith(args[0])) {
                arguments.add("head");
            }
            if (hasPerm(sender, "kill") && "kill".startsWith(args[0])) {
                arguments.add("kill");
            }
            if (hasPerm(sender, "revive") && "revive".startsWith(args[0])) {
                arguments.add("revive");
            }
            if (sender instanceof Player && AwakenLife.getInstance().getConfig().getBoolean("allow-transfer-heads") && "transfer ".startsWith(args[0])) {
                arguments.add("transfer");
            }
            if ("help".startsWith(args[0])) {
                arguments.add("help");
            }
            
        } else if (args.length == 2) {
            if (hasPerm(sender, "head") && sender instanceof Player) {
                if (args[0].equalsIgnoreCase("head")) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        arguments.add(p.getName());
                    }
                }
            }
            if (hasPerm(sender, "kill")) {
                if (args[0].equalsIgnoreCase("kill")) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        arguments.add(p.getName());
                    }
                }
            }
            if (sender instanceof Player) {
                if (args[0].equalsIgnoreCase("transfer") && AwakenLife.getInstance().getConfig().getBoolean("allow-transfer-heads") && "transfer".startsWith(args[1])) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        arguments.add(p.getName());
                    }
                }
            }
        }
        
        return arguments;
    }
    
    
    private static boolean checkArgs(String[] args, CommandSender sender) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Received too little arguments");
            sender.sendMessage(ChatColor.RED + "/awaken help");
            return false;
        }
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + "Mention a player to execute this command on");
            sender.sendMessage(ChatColor.RED + "/awaken " + args[0] + " <playerName>");
            return false;
        }
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Received too many arguments");
            sender.sendMessage(ChatColor.RED + "/awaken " + args[0] + " <playerName>");
            return false;
        }
        return true;
    }
    
    private static boolean hasPerm(CommandSender sender, String permission) {
        String perm = "awakenlife.awaken." + permission;
        if (sender.isOp()) { return true; }
        if (sender.hasPermission("awakenlife")) { return true; }
        if (sender.hasPermission("awakenlife.awaken")) { return true; }
        if (sender.hasPermission("awakenlife.all")) { return true; }
        if (sender.hasPermission("awakenlife.awaken.all")) { return true; }
        if (sender.hasPermission(perm)) { return true; }
        return false;
    }
    
    private static boolean isPlayer(CommandSender sender) {
        return (sender instanceof Player);
    }
}
