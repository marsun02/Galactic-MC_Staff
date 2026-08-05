package com.marsun02.plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import com.marsun02.plugin.punishments.BanCommand;
import com.marsun02.plugin.punishments.KickCommand;
import com.marsun02.plugin.punishments.MuteCommand;
import com.marsun02.plugin.punishments.UnbanCommand;
import com.marsun02.plugin.punishments.UnmuteCommand;
import com.marsun02.plugin.punishments.WarnCommand;
import com.marsun02.plugin.punishments.WarnListener;
import com.marsun02.plugin.staffChat.StaffChat;
import com.marsun02.plugin.staffChat.StaffChatListener;

public class Main extends JavaPlugin {
    private final Set<UUID> staffChatToggled = new HashSet<>();
    private final Map<String, Long> mutedPlayers = new HashMap<>();
    private final Map<String, String> mutedReasons = new HashMap<>();
    private final Map<String, Long> warnedPlayers = new HashMap<>();
    private final Map<String, String> warnedReasons = new HashMap<>();
    
    @Override
    public void onEnable(){
        // Load config
        saveDefaultConfig();
        loadStaffChatStates();
        loadMutedPlayers();
        loadWarnedPlayers();

        // Register StaffChat command
        getCommand("staffchat").setExecutor(new StaffChat(staffChatToggled));
        
        // Register punishment commands
        getCommand("kick").setExecutor(new KickCommand());
        getCommand("ban").setExecutor(new BanCommand());
        getCommand("unban").setExecutor(new UnbanCommand());
        getCommand("mute").setExecutor(new MuteCommand(mutedPlayers, mutedReasons));
        getCommand("unmute").setExecutor(new UnmuteCommand(mutedPlayers, mutedReasons));
        getCommand("warn").setExecutor(new WarnCommand(warnedPlayers, warnedReasons));
        
        // Register StaffChat listener
        getServer().getPluginManager().registerEvents(new StaffChatListener(staffChatToggled, mutedPlayers, mutedReasons), this);
        // Register warn listener
        getServer().getPluginManager().registerEvents(new WarnListener(warnedPlayers, warnedReasons), this);

        getLogger().info("Galactic-MC_Staff er aktivert");
    }

    @Override
    public void onDisable() {
        saveStaffChatStates();
        saveMutedPlayers();
        saveWarnedPlayers();
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
        ConfigurationSection section = config.getConfigurationSection("muted");
        if (section == null) return;
        for (String name : section.getKeys(false)) {
            long expiry = config.getLong("muted." + name + ".expiry", -1L);
            String reason = config.getString("muted." + name + ".reason", "No reason provided.");
            mutedPlayers.put(name.toLowerCase(), expiry);
            mutedReasons.put(name.toLowerCase(), reason);
        }
    }

    private void saveMutedPlayers() {
        FileConfiguration config = getConfig();
        // clear previous muted section
        config.set("muted", null);
        for (var entry : mutedPlayers.entrySet()) {
            String name = entry.getKey();
            long expiry = entry.getValue();
            config.set("muted." + name + ".expiry", expiry);
            config.set("muted." + name + ".reason", mutedReasons.getOrDefault(name, "No reason provided."));
        }
        saveConfig();
    }
    
    private void loadWarnedPlayers() {
        FileConfiguration config = getConfig();
        ConfigurationSection section = config.getConfigurationSection("warns");
        if (section == null) return;
        for (String name : section.getKeys(false)) {
            long expiry = config.getLong("warns." + name + ".expiry", -1L);
            String reason = config.getString("warns." + name + ".reason", "No reason provided.");
            warnedPlayers.put(name.toLowerCase(), expiry);
            warnedReasons.put(name.toLowerCase(), reason);
        }
    }

    private void saveWarnedPlayers() {
        FileConfiguration config = getConfig();
        config.set("warns", null);
        for (var entry : warnedPlayers.entrySet()) {
            String name = entry.getKey();
            long expiry = entry.getValue();
            config.set("warns." + name + ".expiry", expiry);
            config.set("warns." + name + ".reason", warnedReasons.getOrDefault(name, "No reason provided."));
        }
        saveConfig();
    }
    
}
