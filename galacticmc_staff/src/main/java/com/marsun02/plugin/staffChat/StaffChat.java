package com.marsun02.plugin.staffChat;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class StaffChat implements CommandExecutor {

    private final Set<UUID> staffChatToggled;

    public StaffChat(Set<UUID> staffChatToggled) {
        this.staffChatToggled = staffChatToggled;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        // /staffchat                   -> toggle self (must be a player and have server.staff.staffchat permission)
        // /staffchat <player>          -> toggle another player (must have server.staff.staffchat.others permission or console)

        if (args.length == 0) {
            // Toggle self
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Specify a player with: /staffchat <player>");
                return true;
            }

            Player player = (Player) sender;

            if (!(player.hasPermission("server.staff.staffchat"))) {
                player.sendMessage(ChatColor.RED + "Command not found.");
                return true;
            }

            // Toggle staffchat using UUID
            if (staffChatToggled.contains(player.getUniqueId())) {
                staffChatToggled.remove(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Staff chat mode disabled.");
            } else {
                staffChatToggled.add(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Staff chat mode enabled.");
            }

            return true;
        }

        // args.length >= 1 -> toggle another player
        // permission required to modify others
        if (!(sender.hasPermission("server.staff.staffchat.others"))) {
            sender.sendMessage(ChatColor.RED + "Command not found.");
            return true;
        }

        String targetName = args[0];
        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " not found.");
            return true;
        }

        if (staffChatToggled.contains(target.getUniqueId())) {
            staffChatToggled.remove(target.getUniqueId());
            sender.sendMessage(ChatColor.RED + "Staffchat disabled for " + target.getName() + ".");
        } else {
            staffChatToggled.add(target.getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "Staffchat enabled for " + target.getName() + ".");
        }

        return true;

    }
}
