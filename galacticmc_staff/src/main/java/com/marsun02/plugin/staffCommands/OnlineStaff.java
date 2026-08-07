package com.marsun02.plugin.staffCommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class OnlineStaff implements CommandExecutor {

    private static final String PERMISSION = "server.staff.staffmember";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof org.bukkit.command.ConsoleCommandSender) && !sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            StringBuilder onlineStaffList = new StringBuilder(ChatColor.GOLD + "Online Staff Members:\n");
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(PERMISSION)) {
                    String location = getLocationLabel(player);
                    onlineStaffList.append(ChatColor.YELLOW)
                        .append(player.getName())
                        .append(ChatColor.YELLOW)
                        .append(" | ")
                        .append(ChatColor.YELLOW)
                        .append(location)
                        .append('\n');
                }
            }
            sender.sendMessage(onlineStaffList.toString());
            return true;
        }

        return true;
    }

    private String getLocationLabel(Player player) {
        String worldName = player.getWorld().getName();
        if (worldName == null || worldName.isBlank()) {
            return "unknown";
        }
        return worldName;
    }
}
