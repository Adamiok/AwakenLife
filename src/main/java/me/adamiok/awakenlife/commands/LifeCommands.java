package me.adamiok.awakenlife.commands;

import me.adamiok.awakenlife.AwakenLife;
import me.adamiok.awakenlife.data.LifeData;
import me.adamiok.awakenlife.data.MojangApi;
import me.adamiok.awakenlife.items.LifeItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
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

public class LifeCommands implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // help, revive, setHearts, addHearts, removeHearts, getHearts, withdraw, item
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Too little arguments    /life help");
            return true;
        }
        String args0 = args[0];
        
        if (args0.equalsIgnoreCase("help")) {
            // life help
            sender.sendMessage(ChatColor.GOLD + "### Awaken-Life Life Help ###");
            sender.sendMessage(ChatColor.AQUA + "/life help" + ChatColor.WHITE + " : Displays this message");
            sender.sendMessage(ChatColor.AQUA + "/life withdraw" + ChatColor.WHITE + " : Withdraws hearts from you");
            
            if (hasPerm(sender, "item") || hasPerm(sender, "revive") || hasPerm(sender, "sethearts") || hasPerm(sender, "gethearts")) {
                sender.sendMessage(ChatColor.GOLD + "## Admin Commands ##");
            } else {
                return true;
            }
            if (hasPerm(sender, "item")) {
                sender.sendMessage(ChatColor.AQUA + "/life item <item> [amount]" + ChatColor.WHITE + " : Gives you an item. Only valid options: heart, heartStone");
            }
            if (hasPerm(sender, "revive"))  {
                sender.sendMessage(ChatColor.AQUA + "/life revive <playerName>" + ChatColor.WHITE + " : Revives a player banned by running out of hearts");
            }
            if (hasPerm(sender, "sethearts")) {
                sender.sendMessage(ChatColor.AQUA + "/life setHearts <playerName> <amount>" + ChatColor.WHITE + " : Sets the hearts of the given player");
                sender.sendMessage(ChatColor.AQUA + "/life addHearts <playerName> <amount>" + ChatColor.WHITE + " : Adds the amount of hearts to the given player");
                sender.sendMessage(ChatColor.AQUA + "/life removeHearts <playerName> <amount>" + ChatColor.WHITE + " : Removes the amount of hearts from the given player");
            }
            if (hasPerm(sender, "gethearts")) {
                sender.sendMessage(ChatColor.AQUA + "/life getHearts <playerName>" + ChatColor.WHITE + " : Gets the hearts of the player");
            }
            
        } else if (args0.equalsIgnoreCase("revive")) {
            // life revive <playerName>
            if (!hasPerm(sender, "revive")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Received too little arguments");
                sender.sendMessage(ChatColor.RED + "/life revive <playerName>");
                return true;
            }
            if (args.length > 2) {
                sender.sendMessage(ChatColor.RED + "Received too many arguments");
                sender.sendMessage(ChatColor.RED + "/life revive <playerName>");
                return true;
            }
            String revive = args[1];
    
            Bukkit.getScheduler().runTaskAsynchronously(AwakenLife.getInstance(), ()-> {
                try {
                    UUID uuid = MojangApi.playerNameToUuid(revive);
                    if (!LifeData.isBanned(uuid)) {
                        sender.sendMessage(ChatColor.RED + "Player is not banned by losing all hearts");
                        return;
                    }
                    LifeData.removePlayer(uuid);
                    sender.sendMessage(ChatColor.GREEN + "Successfully revived player");
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + "A connection error has occurred while connecting the Mojang servers, try again in a few minutes");
                } catch (InvalidParameterException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid player name");
                }
            });
        } else if (args0.equalsIgnoreCase("setHearts")) {
            if (!hasPerm(sender, "sethearts")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command");
                return true;
            }
            if (!isValidHeartCmd(sender, args)) {
                return true;
            }
            String name = args[1];
            int amount = Integer.parseInt(args[2]) * 2;
            Player player = Bukkit.getPlayer(name);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player not found, make sure the player is online");
                return true;
            }
            if (amount < 2) {
                sender.sendMessage(ChatColor.RED + "Player must have at least 1 heart left");
                return true;
            }
    
            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attribute == null) {
                sender.sendMessage(ChatColor.RED + "Make sure player is online");
                return true;
            }
            attribute.setBaseValue(amount);
            if (amount == 2) {
                sender.sendMessage(ChatColor.GREEN + player.getName() + " has now 1 heart");
                return true;
            }
            sender.sendMessage(ChatColor.GREEN + player.getName() + " has now " + (int) amount/2 + " hearts");
            
        } else if (args0.equalsIgnoreCase("addHearts")) {
            if (!hasPerm(sender, "sethearts")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command");
                return true;
            }
            if (!isValidHeartCmd(sender, args)) {
                return true;
            }
            String name = args[1];
            int amount = Integer.parseInt(args[2]) * 2;
            Player player = Bukkit.getPlayer(name);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player not found, make sure the player is online");
                return true;
            }
            
            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attribute == null) {
                sender.sendMessage(ChatColor.RED + "Make sure player is online");
                return true;
            }
            attribute.setBaseValue(attribute.getValue() + amount);
            
            sender.sendMessage(ChatColor.GREEN + player.getName() + " has now " + (int) attribute.getValue()/2 + " hearts");
            
        } else if (args0.equalsIgnoreCase("removeHearts")) {
            if (!hasPerm(sender, "sethearts")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command");
                return true;
            }
            if (!isValidHeartCmd(sender, args)) {
                return true;
            }
            String name = args[1];
            int amount = Integer.parseInt(args[2]) * 2;
            Player player = Bukkit.getPlayer(name);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player not found, make sure the player is online");
                return true;
            }
    
            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attribute == null) {
                sender.sendMessage(ChatColor.RED + "Make sure player is online");
                return true;
            }
            if ((attribute.getValue() - amount) <= 1) {
                sender.sendMessage(ChatColor.RED + "Player needs to have at least 1 heart. You can maximally remove " + (int) (attribute.getValue()/2-1) + " hearts");
                return true;
            }
            
            attribute.setBaseValue(attribute.getValue() - amount);
            if (amount == 2) {
                sender.sendMessage(ChatColor.GREEN + "Removed 1 heart from " + player.getName());
                return true;
            }
            sender.sendMessage(ChatColor.GREEN + "Removed " + (int) amount /2 + " hearts from " + player.getName());
            
        } else if (args0.equalsIgnoreCase("getHearts")) {
            // (life) getHearts <playerName>
            if (!hasPerm(sender, "gethearts")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command");
                return true;
            }
            if (!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "This command is for operators only");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Received too little arguments");
                sender.sendMessage(ChatColor.RED + "/life " + args[0] + " <playerName>");
                return true;
            }
            if (args.length > 2) {
                sender.sendMessage(ChatColor.RED + "Received too many arguments");
                sender.sendMessage(ChatColor.RED + "/life " + args[0] + " <playerName>");
                return true;
            }
            
            String name = args[1];
            Player player = Bukkit.getPlayer(name);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player not found, make sure the player is online");
                return true;
            }
            
            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attribute == null) {
                sender.sendMessage(ChatColor.RED + "Make sure player is online");
                return true;
            }
            int hearts = (int) (attribute.getValue() / 2);
            sender.sendMessage("Player " + ChatColor.AQUA + player.getName() + ChatColor.WHITE + " has " + ChatColor.RED + hearts + ChatColor.WHITE + " hearts");
            
        } else if (args0.equalsIgnoreCase("withdraw")) {
            // (life) withdraw <amount>
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Received too little arguments");
                sender.sendMessage(ChatColor.RED + "/life " + args[0] + " <amount>");
                return true;
            }
            if (args.length > 2) {
                sender.sendMessage(ChatColor.RED + "Received too many arguments");
                sender.sendMessage(ChatColor.RED + "/life " + args[0] + " <amount>");
                return true;
            }
            
            Player player = (Player) sender;
            int amount;
            try {
                amount = Integer.parseInt(args[1]) * 2;
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "<amount> must be a whole number");
                return true;
            }
            
            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attribute == null) {
                sender.sendMessage(ChatColor.RED + "An error has occurred, please leave and join back");
                return true;
            }
            int health = (int) attribute.getValue();
            int left = health - amount;
            
            if (left <= 1) {
                sender.sendMessage(ChatColor.RED + "Can't withdraw, you must stay least at 1 heart");
                return true;
            }
            
            attribute.setBaseValue(left);
            for (int i = 0; i < amount/2; i++) {
                HashMap<Integer, ItemStack> map = player.getInventory().addItem(LifeItems.HEART);
                Location location = player.getLocation();
                for (ItemStack itemStack : map.values()) {
                    player.getWorld().dropItem(location, itemStack);
                }
            }
            player.sendMessage(ChatColor.GREEN + "Successfully withdrew " + (int) amount/2 + " hearts");
            
        } else if (args0.equalsIgnoreCase("item")) {
            // (life) give <item> [amount]
            if (!hasPerm(sender, "item")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Received too little arguments");
                sender.sendMessage(ChatColor.RED + "/life " + args[0] + " <item> [amount]");
                return true;
            }
            if (args.length > 3) {
                sender.sendMessage(ChatColor.RED + "Received too many arguments");
                sender.sendMessage(ChatColor.RED + "/life " + args[0] + " <item> [amount]");
                return true;
            }
            
            Player player = (Player) sender;
            String item = args[1];
            int amount = 1;
            if (args.length == 3) {
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "[amount] must be a whole number");
                    return true;
                }
            }
    
            ItemStack itemStack;
            if (item.equalsIgnoreCase("heart")) {
                itemStack = LifeItems.HEART;
            } else if (item.equalsIgnoreCase("heartStone") ||
                    item.equalsIgnoreCase("heart_stone") ||
                    item.equalsIgnoreCase("heart-stone")) {
                itemStack = LifeItems.HEART_STONE;
            } else {
                player.sendMessage(ChatColor.RED + "Invalid option for <item>");
                player.sendMessage(ChatColor.RED + "Allowed options: heart, heartStone");
                return true;
            }
            for (int i = 0; i < amount; i++) {
                HashMap<Integer, ItemStack> map = player.getInventory().addItem(itemStack);
                Location location = player.getLocation();
                for (ItemStack drop : map.values()) {
                    player.getWorld().dropItem(location, drop);
                }
            }
            if (amount == 1) {
                player.sendMessage(ChatColor.GREEN + "Added 1 " + item + " to you inventory");
                return true;
            }
            player.sendMessage(ChatColor.GREEN + "Added " + amount + " " + item + "s to you inventory");
            
        } else {
            sender.sendMessage(ChatColor.RED + "This is not a valid option   /life help");
        }
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> arguments = new ArrayList<>();
        
        if (args.length == 1) {
            if (hasPerm(sender, "item") && sender instanceof Player && "item".startsWith(args[0])) {
                arguments.add("item");
            }
            if (hasPerm(sender, "revive") && "revive".startsWith(args[0])) {
                arguments.add("revive");
            }
            if (hasPerm(sender, "sethearts")) {
                if ("setHearts".startsWith(args[0])) {
                    arguments.add("setHearts");
                }
                if ("addHearts".startsWith(args[0])) {
                    arguments.add("addHearts");
                }
                if ("removeHearts".startsWith(args[0])) {
                    arguments.add("removeHearts");
                }
            }
            if (hasPerm(sender, "gethearts") && "getHearts".startsWith(args[0])) {
                arguments.add("getHearts");
            }
            if (sender instanceof Player && "withdraw".startsWith(args[0])) {
                arguments.add("withdraw");
            }
            if ("help".startsWith(args[0])) {
                arguments.add("help");
            }
            
        } else if (args.length == 2) {
            if (hasPerm(sender, "item") && sender instanceof Player) {
                if (args[0].equalsIgnoreCase("item")) {
                    arguments.add("heart");
                    arguments.add("heartStone");
                }
            }
            if (hasPerm(sender, "sethearts") &&
                    (args[0].equalsIgnoreCase("setHearts") ||
                    args[0].equalsIgnoreCase("addHearts") ||
                    args[0].equalsIgnoreCase("removeHearts"))) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    arguments.add(p.getName());
                }
            }
            if (hasPerm(sender, "gethearts")) {
                if (args[0].equalsIgnoreCase("getHearts")) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        arguments.add(p.getName());
                    }
                }
            }
        }
        return arguments;
    }
    
    private static boolean isValidHeartCmd(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Received too little arguments");
            sender.sendMessage(ChatColor.RED + "/life " + args[0] + " <playerName> <amount>");
            return false;
        }
        if (args.length > 3) {
            sender.sendMessage(ChatColor.RED + "Received too many arguments");
            sender.sendMessage(ChatColor.RED + "/life " + args[0] + " <playerName> <amount>");
            return false;
        }
        try {
            Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "<amount> must be a whole number");
            return false;
        }
        return true;
    }
    
    private static boolean hasPerm(CommandSender sender, String permission) {
        String perm = "awakenlife.life." + permission;
        if (sender.isOp()) { return true; }
        if (sender.hasPermission("awakenlife")) { return true; }
        if (sender.hasPermission("awakenlife.all")) { return true; }
        if (sender.hasPermission("awakenlife.life")) { return true; }
        if (sender.hasPermission(perm)) { return true; }
        return false;
    }
}
