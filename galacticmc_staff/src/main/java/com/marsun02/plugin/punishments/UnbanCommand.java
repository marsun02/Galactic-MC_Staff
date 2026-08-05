package com.marsun02.plugin.punishments;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;

public class UnbanCommand implements CommandExecutor {

    private static final String PERMISSION = "server.staff.staffmember";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /unban <player>");
            return true;
        }

        String targetName = args[0];
        BanList banList = Bukkit.getBanList(BanList.Type.PROFILE);

        if (!banList.isBanned(targetName)) {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " is not banned.");
            return true;
        }

        banList.pardon(targetName);

        String announce = ChatColor.GREEN + "[Unban] " + ChatColor.WHITE + sender.getName() + ChatColor.GREEN + " unbanned " + ChatColor.WHITE + targetName + ChatColor.GREEN + ".";

        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.hasPermission(PERMISSION)) {
                player.sendMessage(announce);
            }
        });

        return true;
    }
}
