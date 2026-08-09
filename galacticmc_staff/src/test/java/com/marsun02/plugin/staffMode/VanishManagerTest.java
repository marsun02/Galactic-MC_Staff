package com.marsun02.plugin.staffMode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

public class VanishManagerTest {

    @Test
    public void saveAndLoadStatePersistsVanishedPlayers() {
        VanishManager manager = new VanishManager(null);
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        assertTrue(manager.toggleVanish(uuid, "TestPlayer"));

        FileConfiguration config = new YamlConfiguration();
        manager.saveState(config);

        assertTrue(config.contains("vanished"));
        assertTrue(config.getStringList("vanished").contains("123e4567-e89b-12d3-a456-426614174000|TestPlayer"));

        VanishManager reloadedManager = new VanishManager(null);
        reloadedManager.loadState(config);

        assertTrue(reloadedManager.isVanished(uuid));
        assertEquals("TestPlayer", reloadedManager.getDisplayName(uuid));
    }
}
