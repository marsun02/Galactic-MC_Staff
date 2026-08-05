package com.marsun02.plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.marsun02.plugin.punishments.BanCommand;
import com.marsun02.plugin.punishments.MuteCommand;
import com.marsun02.plugin.punishments.UnbanCommand;
import com.marsun02.plugin.staffChat.StaffChat;
import com.marsun02.plugin.staffChat.StaffChatListener;

public class Main extends JavaPlugin {
    private final Set<UUID> staffChatToggled = new HashSet<>();
    private final Map<String, Long> mutedPlayers = new HashMap<>();
    
    @Override
    public void onEnable(){
        // Load config
        saveDefaultConfig();
        loadStaffChatStates();
        loadMutedPlayers();

        // Register StaffChat command
        getCommand("staffchat").setExecutor(new StaffChat(staffChatToggled));
        
        // Register punishment commands
        getCommand("ban").setExecutor(new BanCommand());
        getCommand("unban").setExecutor(new UnbanCommand());
        getCommand("mute").setExecutor(new MuteCommand(mutedPlayers));
        
        // Register StaffChat listener
        getServer().getPluginManager().registerEvents(new StaffChatListener(staffChatToggled, mutedPlayers), this);

        getLogger().info("Galactic-MC_Staff er aktivert");
    }

    @Override
    public void onDisable() {
        saveStaffChatStates();
        saveMutedPlayers();
        getLogger().info("Galactic-MC_Staff er deaktivert!");
    }

    private void loadStaffChatStates() {
        FileConfiguration config = getConfig();
        List<String> uuidStrings = config.getStringList("staffchat-enabled");
        for (String uuidStr : uuidStrings) {
            try {
                staffChatToggled.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid UUID in config: " + uuidStr);
            }
        }
    }

    private void saveStaffChatStates() {
        FileConfiguration config = getConfig();
        List<String> uuidStrings = staffChatToggled.stream()
            .map(UUID::toString)
            .toList();
        config.set("staffchat-enabled", uuidStrings);
        saveConfig();
    }

    private void loadMutedPlayers() {
        FileConfiguration config = getConfig();
        List<String> entries = config.getStringList("muted-players");
        for (String entry : entries) {
            String[] split = entry.split(":", 2);
            if (split.length != 2) continue;
            try {
                String name = split[0];
                long expiry = Long.parseLong(split[1]);
                mutedPlayers.put(name, expiry);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private void saveMutedPlayers() {
        FileConfiguration config = getConfig();
        List<String> entries = mutedPlayers.entrySet().stream()
            .map(entry -> entry.getKey() + ":" + entry.getValue())
            .toList();
        config.set("muted-players", entries);
        saveConfig();
    }
    
}
