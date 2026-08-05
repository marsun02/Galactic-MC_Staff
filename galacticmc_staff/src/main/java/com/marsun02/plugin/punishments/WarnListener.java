package com.marsun02.plugin.punishments;

import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class WarnListener implements Listener {

    private final Map<String, Long> warnedPlayers;
    private final Map<String, String> warnedReasons;
    private final JavaPlugin plugin;

    public WarnListener(Map<String, Long> warnedPlayers, Map<String, String> warnedReasons, JavaPlugin plugin) {
        this.warnedPlayers = warnedPlayers;
        this.warnedReasons = warnedReasons;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerKey = event.getPlayer().getName().toLowerCase();
        Long expiry = warnedPlayers.get(playerKey);
        if (expiry == null) return;
        if (System.currentTimeMillis() > expiry) {
            warnedPlayers.remove(playerKey);
            warnedReasons.remove(playerKey);
            return;
        }

        String reason = warnedReasons.getOrDefault(playerKey, "No reason provided.");

        // Delay the warn message by a few seconds (3s -> 60 ticks)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Long exp = warnedPlayers.get(playerKey);
            if (exp == null) return; // might have expired or been removed
            if (System.currentTimeMillis() > exp) {
                warnedPlayers.remove(playerKey);
                warnedReasons.remove(playerKey);
                return;
            }
            long remainingMs = exp - System.currentTimeMillis();
            String remaining = formatRemaining(remainingMs);
            event.getPlayer().sendMessage(ChatColor.RED + "[Warn] You have an active warning: " + ChatColor.GRAY + reason + ChatColor.RED + " (expires in " + remaining + ")");
        }, 60L);
    }

    private String formatRemaining(long millis) {
        long seconds = millis / 1000;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (sb.isEmpty()) sb.append((seconds % 60)).append("s");
        return sb.toString().trim();
    }
}
