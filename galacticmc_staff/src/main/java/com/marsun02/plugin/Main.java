package com.marsun02.plugin;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.marsun02.plugin.staffChat.StaffChat;
import com.marsun02.plugin.staffChat.StaffChatListener;

public class Main extends JavaPlugin {
    private final Set<Player> staffChatToggled = new HashSet<>();
    
    @Override
    public void onEnable(){

        // Register StaffChat command
        getCommand("staffchat").setExecutor(new StaffChat(staffChatToggled));

        // Register StaffChat listener
        getServer().getPluginManager().registerEvents(new StaffChatListener(staffChatToggled), this);

        getLogger().info("Galactic-MC_Staff er aktivert");
    }

    @Override
    public void onDisable() {
        getLogger().info("Galactic-MC_Staff er deaktivert!");
    }
    
}
