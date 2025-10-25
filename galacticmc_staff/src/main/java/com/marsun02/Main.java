package com.marsun02;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable(){
        getLogger().info("Galactic-MC_Staff er aktivert");
    }

    @Override
    public void onDisable() {
        getLogger().info("Galactic-MC_Staff er deaktivert!");
    }
    
}
