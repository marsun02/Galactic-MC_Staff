package com.marsun02.plugin.punishments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import net.md_5.bungee.api.ChatColor;

public class HistoryCommand implements CommandExecutor {

    private static final String PERMISSION = "server.staff.staffmember";
    private static final int PAGE_SIZE = 5;
    private final Map<String, List<PunishmentRecord>> punishmentsHistory;

    public HistoryCommand(Map<String, List<PunishmentRecord>> punishmentsHistory) {
        this.punishmentsHistory = punishmentsHistory;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /history <player> [page]");
            return true;
        }

        String targetName = args[0];
        String targetKey = targetName.toLowerCase();
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) page = 1;
            } catch (NumberFormatException ignored) {
                sender.sendMessage(ChatColor.RED + "Page must be a number.");
                return true;
            }
        }

        List<PunishmentRecord> history = punishmentsHistory.getOrDefault(targetKey, new ArrayList<>());
        if (history.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No punishment history found for " + targetName + ".");
            return true;
        }

        List<PunishmentRecord> displayHistory = new ArrayList<>();
        List<PunishmentRecord> meaningfulHistory = new ArrayList<>();
        for (PunishmentRecord record : history) {
            if (isMeaningfulPunishment(record.type)) {
                meaningfulHistory.add(record);
            }
            if (shouldDisplayInHistory(record.type)) {
                displayHistory.add(record);
            }
        }
        Collections.reverse(displayHistory);
        if (displayHistory.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No visible punishment history found for " + targetName + ".");
            return true;
        }

        int totalPages = (int) Math.ceil(meaningfulHistory.size() / (double) PAGE_SIZE);
        if (page > totalPages) page = totalPages;

        sender.sendMessage(ChatColor.RED + "History for " + targetName + " (Limit: " + meaningfulHistory.size() + "):");

        int startIndex = (page - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, displayHistory.size());
        for (int i = startIndex; i < endIndex; i++) {
            PunishmentRecord record = displayHistory.get(i);
            boolean active = isActive(record);
            sender.sendMessage(ChatColor.RED + "-- [" + ChatColor.WHITE + formatDuration(System.currentTimeMillis() - record.createdAt) + " ago" + ChatColor.RED + "] --");
            sender.sendMessage(ChatColor.WHITE + targetName + " was " + ChatColor.RED + record.type + ChatColor.WHITE+ " by " + record.actor + ": '" + ChatColor.RED + record.reason + ChatColor.WHITE + "' [" + (active ? ChatColor.RED + "Active" : ChatColor.DARK_GRAY + "Expired") + ChatColor.WHITE + "]");
            sender.sendMessage((active && record.expiresAt != null ? ChatColor.WHITE + "Expires in " + formatDuration(record.expiresAt - System.currentTimeMillis()) : ""));
        }

        if (totalPages > 1) {
            sender.sendMessage(ChatColor.GRAY + "Page " + page + " of " + totalPages + ". Use /history " + targetName + " <page> to view another page.");
        }

        return true;
    }

    public static void logPunishment(Map<String, List<PunishmentRecord>> punishmentsHistory, String targetKey, String type, String actor, String reason, long createdAt, Long expiresAt, boolean active) {
        punishmentsHistory.computeIfAbsent(targetKey.toLowerCase(), key -> new ArrayList<>())
            .add(new PunishmentRecord(type, actor, reason, createdAt, expiresAt, active));
    }

    private boolean isActive(PunishmentRecord record) {
        if (record.expiresAt == null) {
            return true;
        }
        return System.currentTimeMillis() < record.expiresAt;
    }

    public static boolean shouldDisplayInHistory(String type) {
        return true;
    }

    private boolean isMeaningfulPunishment(String type) {
        return !"unban".equalsIgnoreCase(type) && !"unmute".equalsIgnoreCase(type);
    }

    public static String formatDuration(long durationMillis) {
        long totalMinutes = Math.max(0L, durationMillis / 60_000L);
        long days = totalMinutes / 1440L;
        long hours = (totalMinutes % 1440L) / 60L;
        long minutes = totalMinutes % 60L;

        return days + " " + (days == 1 ? "day" : "days") + ", "
            + hours + " " + (hours == 1 ? "hour" : "hours") + ", "
            + minutes + " " + (minutes == 1 ? "minute" : "minutes");
    }

    public static class PunishmentRecord {
        private final String type;
        private final String actor;
        private final String reason;
        private final long createdAt;
        private final Long expiresAt;
        private final boolean active;

        public PunishmentRecord(String type, String actor, String reason, long createdAt, Long expiresAt, boolean active) {
            this.type = type;
            this.actor = actor;
            this.reason = reason;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.active = active;
        }

        public String getType() {
            return type;
        }

        public String getActor() {
            return actor;
        }

        public String getReason() {
            return reason;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public Long getExpiresAt() {
            return expiresAt;
        }

        public boolean isActive() {
            return active;
        }
    }
}
