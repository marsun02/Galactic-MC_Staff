package com.marsun02.plugin.staffMode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class VanishToggleListener implements Listener {

    private static final long DOUBLE_SNEAK_WINDOW_MS = 400L;

    private final VanishManager vanishManager;
    private final Map<UUID, Long> lastSneakPresses = new HashMap<>();

    public VanishToggleListener(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking() || !vanishManager.isVanished(player)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long lastPress = lastSneakPresses.get(uuid);
        lastSneakPresses.put(uuid, now);

        if (lastPress != null && now - lastPress <= DOUBLE_SNEAK_WINDOW_MS) {
            vanishManager.toggleSpectator(player);
        }
    }
}
