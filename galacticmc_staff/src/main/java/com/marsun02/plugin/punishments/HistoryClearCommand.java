package com.marsun02.plugin.punishments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import com.marsun02.plugin.Main;

import net.md_5.bungee.api.ChatColor;

public class HistoryClearCommand implements CommandExecutor {

    private static final String ADMIN_PERMISSION = "server.staff.admin";
    private final Map<String, List<HistoryCommand.PunishmentRecord>> punishmentsHistory;
    private final Main plugin;
    private final Map<String, Long> warnedPlayers;
    private final Map<String, String> warnedReasons;

    public HistoryClearCommand(Map<String, List<HistoryCommand.PunishmentRecord>> punishmentsHistory, Main plugin,
            Map<String, Long> warnedPlayers, Map<String, String> warnedReasons) {
        this.punishmentsHistory = punishmentsHistory;
        this.plugin = plugin;
        this.warnedPlayers = warnedPlayers;
        this.warnedReasons = warnedReasons;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /history clear <player> [amount]");
            return true;
        }

        String targetName = args[0];
        String targetKey = targetName.toLowerCase();
        int amount = -1;
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount < 1) {
                    sender.sendMessage(ChatColor.RED + "Amount must be at least 1.");
                    return true;
                }
            } catch (NumberFormatException ignored) {
                sender.sendMessage(ChatColor.RED + "Amount must be a number.");
                return true;
            }
        }

        List<HistoryCommand.PunishmentRecord> history = punishmentsHistory.getOrDefault(targetKey, new ArrayList<>());
        if (history.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No punishment history found for " + targetName + ".");
            return true;
        }

        int removedCount = clearRecentHistory(targetKey, amount);
        if (removedCount == 0) {
            sender.sendMessage(ChatColor.RED + "No punishment history entries were removed.");
            return true;
        }

        if (plugin != null) {
            plugin.savePunishmentHistory();
        }

        sender.sendMessage(ChatColor.GREEN + "Successfully cleared " + removedCount + " punishment history entr" + (removedCount == 1 ? "y" : "ies") + " for " + targetName + ".");
        return true;
    }

    private int clearRecentHistory(String targetKey, int amount) {
        List<HistoryCommand.PunishmentRecord> history = punishmentsHistory.get(targetKey);
        if (history == null || history.isEmpty()) {
            return 0;
        }

        int removeCount = amount < 0 ? history.size() : Math.min(amount, history.size());
        boolean removedWarn = false;
        for (int i = 0; i < removeCount; i++) {
            HistoryCommand.PunishmentRecord record = history.remove(history.size() - 1);
            if ("warned".equalsIgnoreCase(record.getType())) {
                removedWarn = true;
            }
        }

        if (history.isEmpty()) {
            punishmentsHistory.remove(targetKey);
        }

        if (removedWarn && warnedPlayers != null && warnedReasons != null) {
            warnedPlayers.remove(targetKey);
            warnedReasons.remove(targetKey);
        }

        return removeCount;
    }
}
