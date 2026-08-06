package com.marsun02.plugin.punishments;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class KickCommand implements CommandExecutor {

    private static final String PERMISSION = "server.staff.staffmember";
    private final Map<String, List<HistoryCommand.PunishmentRecord>> punishmentsHistory;

    public KickCommand(Map<String, List<HistoryCommand.PunishmentRecord>> punishmentsHistory) {
        this.punishmentsHistory = punishmentsHistory;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /kick <player> [reason]");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            target = Bukkit.getPlayer(targetName);
        }

        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " is not online.");
            return true;
        }

        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "No reason provided.";
        String kickMessage = ChatColor.RED + "You have been kicked from the server. \nReason: " + ChatColor.GRAY + reason;
        target.kickPlayer(kickMessage);

        String announce = ChatColor.RED + "[Kick] " + ChatColor.WHITE + sender.getName() + ChatColor.RED + " kicked " + ChatColor.WHITE + target.getName() + ChatColor.RED + ". Reason: " + ChatColor.GRAY + reason;
        HistoryCommand.logPunishment(punishmentsHistory, target.getName().toLowerCase(), "kick", sender.getName(), reason, System.currentTimeMillis(), null, false);
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.hasPermission(PERMISSION)) {
                player.sendMessage(announce);
            }
        });
        return true;
    }
}
