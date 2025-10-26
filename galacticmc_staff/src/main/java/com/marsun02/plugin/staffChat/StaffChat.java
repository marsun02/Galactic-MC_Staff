package com.marsun02.plugin.staffChat;

import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class StaffChat implements CommandExecutor {

    private final Set<Player> staffChatToggled;

    public StaffChat(Set<Player> staffChatToggled) {
        this.staffChatToggled = staffChatToggled;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You need to be a player to perform this command");
        }

        Player player = (Player) sender;

        if (!(player.hasPermission("server.staff.staffchat"))) {
            player.sendMessage(ChatColor.RED + "Command not found.");
            return true;
        }

        // Toggle staff chat mode
        if (staffChatToggled.contains(player)) {
            staffChatToggled.remove(player);
            player.sendMessage(ChatColor.RED + "Staff chat mode disabled.");
        } else {
            staffChatToggled.add(player);
            player.sendMessage(ChatColor.GREEN + "Staff chat mode enabled.");
        }

        return true;

    }
    
}
