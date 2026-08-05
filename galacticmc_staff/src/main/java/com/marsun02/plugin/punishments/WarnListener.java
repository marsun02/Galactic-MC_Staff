package com.marsun02.plugin.punishments;

import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.md_5.bungee.api.ChatColor;

public class WarnListener implements Listener {

    private final Map<String, Long> warnedPlayers;
    private final Map<String, String> warnedReasons;

    public WarnListener(Map<String, Long> warnedPlayers, Map<String, String> warnedReasons) {
        this.warnedPlayers = warnedPlayers;
        this.warnedReasons = warnedReasons;
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
        event.getPlayer().sendMessage(ChatColor.YELLOW + "[Warn] You have an active warning: " + ChatColor.GRAY + reason + ChatColor.YELLOW + " (expires soon)");
    }
}
