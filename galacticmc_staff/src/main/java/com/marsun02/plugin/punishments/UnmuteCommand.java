package com.marsun02.plugin.punishments;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import net.md_5.bungee.api.ChatColor;

public class UnmuteCommand implements CommandExecutor {

    private static final String PERMISSION = "server.staff.staffmember";
    private final Map<String, List<HistoryCommand.PunishmentRecord>> punishmentsHistory;
    private final Map<String, Long> mutedPlayers;
    private final Map<String, String> mutedReasons;

    public UnmuteCommand(Map<String, Long> mutedPlayers, Map<String, String> mutedReasons, Map<String, List<HistoryCommand.PunishmentRecord>> punishmentsHistory) {
        this.mutedPlayers = mutedPlayers;
        this.mutedReasons = mutedReasons;
        this.punishmentsHistory = punishmentsHistory;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /unmute <player>");
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        String canonicalName = target.getName() != null ? target.getName() : targetName;
        String targetKey = canonicalName.toLowerCase();

        if (!mutedPlayers.containsKey(targetKey)) {
            sender.sendMessage(ChatColor.RED + "Player " + canonicalName + " is not muted.");
            return true;
        }

        mutedPlayers.remove(targetKey);
        mutedReasons.remove(targetKey);

        String announce = ChatColor.RED + "[Unmute] " + ChatColor.WHITE + sender.getName() + ChatColor.RED + " unmuted " + ChatColor.WHITE + canonicalName + ChatColor.RED + ".";
        HistoryCommand.logPunishment(punishmentsHistory, targetKey, "unmuted", sender.getName(), "No reason provided.", System.currentTimeMillis(), null, false);

        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.hasPermission(PERMISSION)) {
                player.sendMessage(announce);
            }
        });

        return true;
    }
}
