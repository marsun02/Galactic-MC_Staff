package com.marsun02.plugin.staffChat;

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

    public StaffChatListener(Set<UUID> staffChatToggled) {
        this.staffChatToggled = staffChatToggled;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check if the player has staff chat mode enabled or using shortcut "#"
        if (staffChatToggled.contains(player.getUniqueId()) || message.startsWith("#")) {
            
            // If they use shortcut "#", but don't have permission, this will deny them
            if (!player.hasPermission("server.staff.staffchat")) {
                event.setMessage(message);;
                return;
            }
            
            // Cancel the normal chat event
            event.setCancelled(true);

            // Remove the # prefix if used
            if (message.startsWith("#")) {
                message = message.substring(1).trim();
            }

            // Format and send the message to staff members only
            String formattedMessage = ChatColor.AQUA + player.getName() + " " + ChatColor.AQUA + message;

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("server.staff.staffchat")) {
                    p.sendMessage(formattedMessage);
                }
            }
        }
    }
}
