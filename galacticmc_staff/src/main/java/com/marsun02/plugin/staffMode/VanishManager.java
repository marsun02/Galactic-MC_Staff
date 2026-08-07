package com.marsun02.plugin.staffMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class VanishManager implements Listener {

    private static final String STAFF_PERMISSION = "server.staff.staffmember";
    private static final String ADMIN_PERMISSION = "server.staff.admin";

    private final JavaPlugin plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private final Map<UUID, String> vanishedNames = new HashMap<>();
    private final Map<UUID, Location> originalLocations = new HashMap<>();
    private final Map<UUID, GameMode> originalGameModes = new HashMap<>();
    private final Map<UUID, Boolean> originalFlyingStates = new HashMap<>();
    private final Map<UUID, Boolean> originalAllowFlightStates = new HashMap<>();
    private final Map<UUID, Boolean> spectatorMode = new HashMap<>();

    public VanishManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean toggleVanish(Player player) {
        return toggleVanish(player.getUniqueId(), player.getName());
    }

    public boolean toggleVanish(String targetName) {
        Player onlinePlayer = getOnlinePlayer(targetName);
        UUID uuid = onlinePlayer != null ? onlinePlayer.getUniqueId() : getOfflinePlayerUuid(targetName);
        String resolvedName = onlinePlayer != null ? onlinePlayer.getName() : targetName;
        return toggleVanish(uuid, resolvedName);
    }

    public boolean toggleVanish(UUID uuid, String name) {
        if (vanishedPlayers.contains(uuid)) {
            return disableVanish(uuid, name);
        }
        return enableVanish(uuid, name);
    }

    public boolean enableVanish(Player player) {
        if (!player.hasPermission(STAFF_PERMISSION) && !player.hasPermission(ADMIN_PERMISSION)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use vanish.");
            return false;
        }
        return enableVanish(player.getUniqueId(), player.getName(), player);
    }

    public boolean enableVanish(UUID uuid, String name) {
        Player onlinePlayer = getOnlinePlayer(uuid);
        if (onlinePlayer != null) {
            return enableVanish(uuid, name, onlinePlayer);
        }

        if (vanishedPlayers.contains(uuid)) {
            return true;
        }

        vanishedPlayers.add(uuid);
        vanishedNames.put(uuid, name);
        if (plugin != null) {
            saveState(plugin.getConfig());
            plugin.saveConfig();
        }
        return true;
    }

    public boolean enableVanish(UUID uuid, String name, Player player) {
        if (vanishedPlayers.contains(uuid)) {
            return true;
        }

        originalLocations.put(uuid, player.getLocation().clone());
        originalGameModes.put(uuid, player.getGameMode());
        originalFlyingStates.put(uuid, player.isFlying());
        originalAllowFlightStates.put(uuid, player.getAllowFlight());

        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvulnerable(true);
        player.setGameMode(GameMode.SPECTATOR);

        vanishedPlayers.add(uuid);
        vanishedNames.put(uuid, name);
        updateVisibility(player);
        saveState(plugin.getConfig());
        plugin.saveConfig();
        player.sendMessage(ChatColor.GREEN + "You are now vanished.");
        return true;
    }

    public boolean disableVanish(Player player) {
        return disableVanish(player.getUniqueId(), player.getName(), player);
    }

    public boolean disableVanish(UUID uuid, String name) {
        Player onlinePlayer = getOnlinePlayer(uuid);
        if (onlinePlayer != null) {
            return disableVanish(uuid, name, onlinePlayer);
        }

        if (!vanishedPlayers.contains(uuid)) {
            return false;
        }

        vanishedPlayers.remove(uuid);
        vanishedNames.remove(uuid);
        spectatorMode.remove(uuid);
        originalLocations.remove(uuid);
        originalGameModes.remove(uuid);
        originalFlyingStates.remove(uuid);
        originalAllowFlightStates.remove(uuid);
        if (plugin != null) {
            saveState(plugin.getConfig());
            plugin.saveConfig();
        }
        return true;
    }

    public boolean disableVanish(UUID uuid, String name, Player player) {
        if (!vanishedPlayers.contains(uuid)) {
            return false;
        }

        vanishedPlayers.remove(uuid);
        vanishedNames.remove(uuid);
        updateVisibility(player);

        Location originalLocation = originalLocations.remove(uuid);
        if (originalLocation != null) {
            player.teleport(originalLocation);
        }

        GameMode originalGameMode = originalGameModes.remove(uuid);
        if (originalGameMode != null) {
            player.setGameMode(originalGameMode);
        }

        Boolean wasFlying = originalFlyingStates.remove(uuid);
        if (wasFlying != null) {
            player.setFlying(wasFlying);
        }

        Boolean allowedFlight = originalAllowFlightStates.remove(uuid);
        if (allowedFlight != null) {
            player.setAllowFlight(allowedFlight);
        }

        spectatorMode.remove(uuid);
        player.setInvulnerable(false);
        if (plugin != null) {
            saveState(plugin.getConfig());
            plugin.saveConfig();
        }
        player.sendMessage(ChatColor.YELLOW + "Vanish disabled.");
        return true;
    }

    private Player getOnlinePlayer(UUID uuid) {
        return Bukkit.getServer() != null ? Bukkit.getPlayer(uuid) : null;
    }

    private Player getOnlinePlayer(String name) {
        return Bukkit.getServer() != null ? Bukkit.getPlayerExact(name) : null;
    }

    private UUID getOfflinePlayerUuid(String name) {
        return Bukkit.getServer() != null ? Bukkit.getOfflinePlayer(name).getUniqueId() : UUID.nameUUIDFromBytes(("offline:" + name).getBytes());
    }

    public boolean toggleSpectator(Player player) {
        UUID uuid = player.getUniqueId();
        if (!vanishedPlayers.contains(uuid)) {
            return false;
        }

        boolean current = spectatorMode.getOrDefault(uuid, false);
        spectatorMode.put(uuid, !current);

        if (!current) {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(ChatColor.AQUA + "Switched to spectator mode.");
        } else {
            player.setGameMode(GameMode.SURVIVAL);
            player.sendMessage(ChatColor.AQUA + "Returned to normal mode.");
        }
        return true;
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    public String getDisplayName(UUID uuid) {
        return vanishedNames.get(uuid);
    }

    public void loadState(FileConfiguration config) {
        vanishedPlayers.clear();
        vanishedNames.clear();
        List<String> entries = config.getStringList("vanished");
        for (String entry : entries) {
            String[] parts = entry.split("\\|", 2);
            if (parts.length == 0) {
                continue;
            }
            try {
                UUID uuid = UUID.fromString(parts[0]);
                String name = parts.length > 1 ? parts[1] : "Unknown";
                vanishedPlayers.add(uuid);
                vanishedNames.put(uuid, name);
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid entries.
            }
        }
    }

    public void saveState(FileConfiguration config) {
        List<String> entries = new ArrayList<>();
        for (UUID uuid : vanishedPlayers) {
            String name = vanishedNames.getOrDefault(uuid, "Unknown");
            entries.add(uuid.toString() + "|" + name);
        }
        config.set("vanished", entries);
    }

    public void updateVisibility(Player player) {
        if (plugin == null) {
            return;
        }

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (vanishedPlayers.contains(player.getUniqueId())) {
                if (target.hasPermission(ADMIN_PERMISSION) || target.hasPermission(STAFF_PERMISSION)) {
                    target.showPlayer(plugin, player);
                } else {
                    target.hidePlayer(plugin, player);
                }
            } else {
                target.showPlayer(plugin, player);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (vanishedPlayers.contains(player.getUniqueId())) {
            applyStoredVanishState(player);
            updateVisibility(player);
            return;
        }

        for (UUID vanished : vanishedPlayers) {
            Player vanishedPlayer = Bukkit.getPlayer(vanished);
            if (vanishedPlayer != null && !player.hasPermission(ADMIN_PERMISSION) && !player.hasPermission(STAFF_PERMISSION)) {
                player.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!vanishedPlayers.contains(uuid)) {
            spectatorMode.remove(uuid);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (vanishedPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    private void applyStoredVanishState(Player player) {
        UUID uuid = player.getUniqueId();
        originalLocations.put(uuid, player.getLocation().clone());
        originalGameModes.put(uuid, player.getGameMode());
        originalFlyingStates.put(uuid, player.isFlying());
        originalAllowFlightStates.put(uuid, player.getAllowFlight());

        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvulnerable(true);
        player.setGameMode(GameMode.SPECTATOR);
    }
}
