package com.marsun02.plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.marsun02.plugin.punishments.BanCommand;
import com.marsun02.plugin.staffChat.StaffChat;
import com.marsun02.plugin.staffChat.StaffChatListener;

public class Main extends JavaPlugin {
    private final Set<UUID> staffChatToggled = new HashSet<>();
    
    @Override
    public void onEnable(){
        // Load config
        saveDefaultConfig();
        loadStaffChatStates();

        // Register StaffChat command
        getCommand("staffchat").setExecutor(new StaffChat(staffChatToggled));
        
        // Register ban command
        getCommand("ban").setExecutor(new BanCommand());
        
        // Register StaffChat listener
        getServer().getPluginManager().registerEvents(new StaffChatListener(staffChatToggled), this);

        getLogger().info("Galactic-MC_Staff er aktivert");
    }

    @Override
    public void onDisable() {
        saveStaffChatStates();
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
    
}
