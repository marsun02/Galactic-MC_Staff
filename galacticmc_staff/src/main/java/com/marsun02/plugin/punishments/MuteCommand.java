package com.marsun02.plugin.punishments;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import net.md_5.bungee.api.ChatColor;

public class MuteCommand implements CommandExecutor {

    private static final String PERMISSION = "server.staff.staffmember";
    private final Map<String, Long> mutedPlayers;
    private final Map<String, String> mutedReasons;

    public MuteCommand(Map<String, Long> mutedPlayers, Map<String, String> mutedReasons) {
        this.mutedPlayers = mutedPlayers;
        this.mutedReasons = mutedReasons;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /mute <player> [amount] [seconds | minutes | hours | days] [reason]");
            return true;
        }

        String targetName = args[0];
        String targetKey = targetName.toLowerCase();
        long maxAllowed = getMaxAllowedSeconds(sender);
        long durationSeconds = -1L;
        int reasonIndex = 1;

        if (args.length == 1) {
            durationSeconds = maxAllowed == Long.MAX_VALUE ? -1L : maxAllowed;
        } else if (args.length >= 3 && BanCommand.isInteger(args[1]) && BanCommand.isUnit(args[2])) {
            durationSeconds = BanCommand.parseDurationSeconds(args[1], args[2]);
            reasonIndex = 3;
        } else if (args.length >= 2 && BanCommand.isDurationToken(args[1])) {
            durationSeconds = BanCommand.parseDurationSecondsToken(args[1]);
            reasonIndex = 2;
        } else {
            durationSeconds = maxAllowed == Long.MAX_VALUE ? -1L : maxAllowed;
            reasonIndex = 1;
        }

        String reason = args.length > reasonIndex
                ? String.join(" ", java.util.Arrays.copyOfRange(args, reasonIndex, args.length))
                : "No reason provided.";

        boolean permanent = durationSeconds <= 0;
        if (permanent && maxAllowed != Long.MAX_VALUE) {
            sender.sendMessage(ChatColor.RED + "You are not allowed to issue permanent mutes.");
            return true;
        }

        if (!permanent && maxAllowed != Long.MAX_VALUE && durationSeconds > maxAllowed) {
            sender.sendMessage(ChatColor.RED + "You may only mute up to " + formatDuration(maxAllowed) + ".");
            return true;
        }

        long expiry = permanent ? -1L : System.currentTimeMillis() + durationSeconds * 1000L;
        mutedPlayers.put(targetKey, expiry);
        mutedReasons.put(targetKey, reason);

        String durationText = durationSeconds > 0 ? formatDuration(durationSeconds) : "permanently";
        String announce = ChatColor.RED + "[Mute] " + ChatColor.WHITE + sender.getName() + ChatColor.RED + " muted " + ChatColor.WHITE + targetName + ChatColor.RED + " " + durationText + " for: " + ChatColor.GRAY + reason;

        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.hasPermission(PERMISSION)) {
                player.sendMessage(announce);
            }
        });

        // One-time on-screen title for the muted player if online
        org.bukkit.entity.Player online = Bukkit.getPlayerExact(targetName);
        if (online != null) {
            String title = ChatColor.RED + "You are muted";
            String subtitle = ChatColor.GRAY + reason + ChatColor.RED + " | " + ChatColor.GRAY + (permanent ? "permanently" : durationText);
            try {
                online.sendTitle(title, subtitle, 10, 60, 10);
            } catch (NoSuchMethodError ignored) {
                // Older server versions may not support titles; fall back to chat message
                online.sendMessage(title + " - " + subtitle);
            }
        }

        return true;
    }

    private long getMaxAllowedSeconds(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) return Long.MAX_VALUE;
        if (sender.hasPermission("server.staff.owner")) return Long.MAX_VALUE;
        if (sender.hasPermission("server.staff.admin")) return Long.MAX_VALUE;
        if (sender.hasPermission("server.staff.mod")) return 30L * 24L * 60L * 60L;
        if (sender.hasPermission("server.staff.helper")) return 14L * 24L * 60L * 60L;
        return -1L;
    }

    private String formatDuration(long durationSeconds) {
        if (durationSeconds < 60L) {
            return durationSeconds + " second(s)";
        }
        if (durationSeconds < 3600L) {
            return (durationSeconds / 60L) + " minute(s)";
        }
        if (durationSeconds < 86400L) {
            return (durationSeconds / 3600L) + " hour(s)";
        }
        return (durationSeconds / 86400L) + " day(s)";
    }
}
