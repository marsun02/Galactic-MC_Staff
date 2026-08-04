package com.marsun02.plugin.punishments;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class BanCommand implements CommandExecutor {

    private static final String PERMISSION = "server.staff.staffmember";
    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)\\s*(s|sec|secs|seconds|m|min|mins|minutes|h|hr|hrs|hours|d|day|days)$", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /ban <player> [amount] [seconds|minutes|hours|days] [reason]");
            return true;
        }

        String targetName = args[0];
        long durationSeconds = -1L;
        int reasonIndex = 1;

        if (args.length >= 3 && isInteger(args[1]) && isUnit(args[2])) {
            durationSeconds = parseDurationSeconds(args[1], args[2]);
            reasonIndex = 3;
        } else if (args.length >= 2) {
            String singleToken = args[1];
            if (isDurationToken(singleToken)) {
                durationSeconds = parseDurationSecondsToken(singleToken);
                reasonIndex = 2;
            }
        }

        String reason = args.length > reasonIndex
                ? String.join(" ", Arrays.copyOfRange(args, reasonIndex, args.length))
                : "No reason provided.";

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " was not found.");
            return true;
        }

        Date expires = durationSeconds > 0 ? Date.from(Instant.now().plusSeconds(durationSeconds)) : null;
        Bukkit.getBanList(BanList.Type.PROFILE).addBan(targetName, reason, expires, sender.getName());

        if (target.isOnline()) {
            Player onlinePlayer = Bukkit.getPlayerExact(targetName);
            if (onlinePlayer != null) {
                onlinePlayer.kickPlayer(ChatColor.RED + "You have been banned: " + reason);
            }
        }

        String durationText = durationSeconds > 0 ? formatDuration(durationSeconds) : "permanently";
        sender.sendMessage(ChatColor.GREEN + "Player " + targetName + " has been banned " + durationText + " for: " + reason);
        return true;
    }

    static long parseDurationSeconds(String amountText, String unitText) {
        long amount = Long.parseLong(amountText);
        String unit = unitText.toLowerCase();

        return switch (unit) {
            case "second", "seconds", "sec", "secs" -> amount;
            case "minute", "minutes", "min", "mins" -> amount * 60L;
            case "hour", "hours", "hr", "hrs" -> amount * 60L * 60L;
            case "day", "days" -> amount * 24L * 60L * 60L;
            default -> throw new IllegalArgumentException("Unsupported unit: " + unitText);
        };
    }

    static long parseDurationSecondsToken(String input) {
        Matcher matcher = DURATION_PATTERN.matcher(input.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid duration: " + input);
        }

        long amount = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2).toLowerCase();

        return switch (unit) {
            case "s", "sec", "secs", "seconds" -> amount;
            case "m", "min", "mins", "minutes" -> amount * 60L;
            case "h", "hr", "hrs", "hours" -> amount * 60L * 60L;
            case "d", "day", "days" -> amount * 24L * 60L * 60L;
            default -> throw new IllegalArgumentException("Unsupported unit: " + unit);
        };
    }

    private boolean isInteger(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isUnit(String value) {
        String unit = value.toLowerCase();
        return switch (unit) {
            case "second", "seconds", "sec", "secs", "minute", "minutes", "min", "mins", "hour", "hours", "hr", "hrs", "day", "days" -> true;
            default -> false;
        };
    }

    private boolean isDurationToken(String value) {
        return DURATION_PATTERN.matcher(value.trim()).matches();
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
