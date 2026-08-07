package com.marsun02.plugin.staffMode;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class VanishToggleListener implements Listener {

    private final VanishManager vanishManager;

    public VanishToggleListener(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }

        if (!vanishManager.isVanished(player)) {
            return;
        }

        if (player.isOnGround()) {
            return;
        }

        vanishManager.toggleSpectator(player);
    }
}
