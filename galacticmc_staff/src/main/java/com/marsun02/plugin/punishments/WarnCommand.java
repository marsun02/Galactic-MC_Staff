package com.marsun02.plugin.punishments;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import net.md_5.bungee.api.ChatColor;

public class WarnCommand implements CommandExecutor {

    private static final String PERMISSION = "server.staff.staffmember";
    private final Map<String, Long> warnedPlayers;
    private final Map<String, String> warnedReasons;

    public WarnCommand(Map<String, Long> warnedPlayers, Map<String, String> warnedReasons) {
        this.warnedPlayers = warnedPlayers;
        this.warnedReasons = warnedReasons;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /warn <player> <reason>");
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        String canonicalName = target.getName() != null ? target.getName() : targetName;
        String targetKey = canonicalName.toLowerCase();

        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        long sevenDaysMs = 7L * 24L * 60L * 60L * 1000L;
        long expiry = System.currentTimeMillis() + sevenDaysMs;

        warnedPlayers.put(targetKey, expiry);
        warnedReasons.put(targetKey, reason);

        String announce = ChatColor.RED + "[Warn] " + ChatColor.WHITE + sender.getName() + ChatColor.RED + " warned " + ChatColor.WHITE + canonicalName + ChatColor.RED + " for: " + ChatColor.GRAY + reason;

        // Notify online staff
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.hasPermission(PERMISSION)) {
                player.sendMessage(announce);
            }
        });

        // Notify warned player if online
        if (target.isOnline()) {
            org.bukkit.entity.Player online = Bukkit.getPlayerExact(canonicalName);
            if (online != null) {
                online.sendMessage(ChatColor.YELLOW + "You have been warned: " + ChatColor.GRAY + reason + ChatColor.YELLOW + " (expires in 7 days)");
            }
        }
        return true;
    }
}
 
