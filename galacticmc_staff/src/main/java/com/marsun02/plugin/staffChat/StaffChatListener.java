package com.marsun02.plugin.staffChat;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.md_5.bungee.api.ChatColor;

public class StaffChatListener implements Listener {

    private final Set<UUID> staffChatToggled;
    private final Map<String, Long> mutedPlayers;

    public StaffChatListener(Set<UUID> staffChatToggled, Map<String, Long> mutedPlayers) {
        this.staffChatToggled = staffChatToggled;
        this.mutedPlayers = mutedPlayers;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        String playerKey = player.getName().toLowerCase();

        boolean isStaffChat = staffChatToggled.contains(player.getUniqueId()) || message.startsWith("#");
        if (isStaffChat) {
            if (!player.hasPermission("server.staff.staffchat")) {
                return;
            }

            event.setCancelled(true);
            if (message.startsWith("#")) {
                message = message.substring(1).trim();
            }

            String formattedMessage = ChatColor.AQUA + player.getName() + " " + ChatColor.AQUA + message;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("server.staff.staffchat")) {
                    p.sendMessage(formattedMessage);
                }
            }
            return;
        }

        if (isPlayerMuted(playerKey)) {
            event.setCancelled(true);
            long expiry = mutedPlayers.getOrDefault(playerKey, -1L);
            if (expiry < 0) {
                player.sendMessage(ChatColor.RED + "You are muted permanently.");
            } else {
                long remaining = expiry - System.currentTimeMillis();
                if (remaining <= 0) {
                    mutedPlayers.remove(playerKey);
                    return;
                }
                player.sendMessage(ChatColor.RED + "You are muted. Time left: " + formatRemaining(remaining));
            }
        }
    }

    private boolean isPlayerMuted(String playerKey) {
        Long expiry = mutedPlayers.get(playerKey);
        if (expiry == null) {
            return false;
        }
        if (expiry < 0) {
            return true;
        }
        if (System.currentTimeMillis() > expiry) {
            mutedPlayers.remove(playerKey);
            return false;
        }
        return true;
    }

    private String formatRemaining(long millis) {
        long seconds = millis / 1000;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }
}
