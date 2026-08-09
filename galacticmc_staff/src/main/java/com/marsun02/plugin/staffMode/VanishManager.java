package com.marsun02.plugin.staffMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import net.md_5.bungee.api.ChatColor;

public class VanishManager implements Listener {

    private static final String STAFF_PERMISSION = "server.staff.staffmember";
    private static final String ADMIN_PERMISSION = "server.staff.admin";
    private static final String STAFF_VISIBILITY_TEAM = "galactic_staff_vis";
    private static final String STAFF_MODE_OBJECTIVE = "staff_mode";
    private static final GameMode STAFF_OBSERVER_MODE = GameMode.SPECTATOR;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");

    private final JavaPlugin plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private final Map<UUID, String> vanishedNames = new HashMap<>();
    private final Map<UUID, Location> originalLocations = new HashMap<>();
    private final Map<UUID, GameMode> originalGameModes = new HashMap<>();
    private final Map<UUID, Boolean> originalFlyingStates = new HashMap<>();
    private final Map<UUID, Boolean> originalAllowFlightStates = new HashMap<>();
    private final Map<UUID, Boolean> spectatorMode = new HashMap<>();
    private final Map<UUID, ArmorStand> spectatorProxies = new HashMap<>();
    private final Map<UUID, UUID> staffTargets = new HashMap<>();
    private final Map<UUID, Scoreboard> previousScoreboards = new HashMap<>();
    private final BukkitTask spectatorProxyTask;
    private final BukkitTask staffScoreboardTask;

    public VanishManager(JavaPlugin plugin) {
        this.plugin = plugin;
        if (plugin != null) {
            this.spectatorProxyTask = new BukkitRunnable() {
                @Override
                public void run() {
                    refreshSpectatorProxies();
                }
            }.runTaskTimer(plugin, 1L, 2L);
            this.staffScoreboardTask = new BukkitRunnable() {
                @Override
                public void run() {
                    refreshStaffModeScoreboards();
                }
            }.runTaskTimer(plugin, 1L, 20L);
        } else {
            this.spectatorProxyTask = null;
            this.staffScoreboardTask = null;
        }
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
        if (onlinePlayer != null) {
            showStaffModeScoreboard(onlinePlayer);
        }
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

        GameMode originalMode = player.getGameMode();
        originalLocations.put(uuid, player.getLocation().clone());
        originalGameModes.put(uuid, originalMode);
        originalFlyingStates.put(uuid, player.isFlying());
        originalAllowFlightStates.put(uuid, player.getAllowFlight());

        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvulnerable(true);
        player.setGameMode(originalMode);

        vanishedPlayers.add(uuid);
        vanishedNames.put(uuid, name);
        showStaffModeScoreboard(player);
        updateVisibility(player);
        if (plugin != null) {
            saveState(plugin.getConfig());
            plugin.saveConfig();
        }
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
        staffTargets.remove(uuid);
        if (onlinePlayer != null) {
            hideStaffModeScoreboard(onlinePlayer);
        }
        removeSpectatorProxy(uuid);
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
        staffTargets.remove(uuid);
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
        hideStaffModeScoreboard(player);
        removeSpectatorProxy(uuid);
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
            player.setGameMode(STAFF_OBSERVER_MODE);
            player.setAllowFlight(true);
            player.setFlying(true);
            createOrUpdateSpectatorProxy(player);
            updateVisibility(player);
            player.sendMessage(ChatColor.AQUA + "Switched to spectator mode.");
        } else {
            GameMode originalMode = originalGameModes.getOrDefault(uuid, GameMode.SURVIVAL);
            player.setGameMode(originalMode);
            player.setAllowFlight(true);
            player.setFlying(true);
            removeSpectatorProxy(uuid);
            updateVisibility(player);
            player.sendMessage(ChatColor.AQUA + "Returned to normal vanish mode.");
        }
        return true;
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    public boolean setTarget(Player staffPlayer, Player targetPlayer, boolean teleportToTarget) {
        if (!isVanished(staffPlayer)) {
            return false;
        }

        staffTargets.put(staffPlayer.getUniqueId(), targetPlayer.getUniqueId());
        if (teleportToTarget) {
            staffPlayer.teleport(targetPlayer.getLocation());
        }
        updateStaffModeScoreboard(staffPlayer);
        return true;
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

        refreshStaffVisibilityTeam();
        refreshSpectatorProxies();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            applyVisibility(viewer, player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (vanishedPlayers.contains(player.getUniqueId())) {
            applyStoredVanishState(player);
            showStaffModeScoreboard(player);
        }

        refreshStaffVisibilityTeam();
        refreshAllVisibility();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        hideStaffModeScoreboard(event.getPlayer());
        staffTargets.remove(uuid);
        removeSpectatorProxy(uuid);
        if (!vanishedPlayers.contains(uuid)) {
            spectatorMode.remove(uuid);
        }

        refreshStaffVisibilityTeam();
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

    @EventHandler
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractPlayer(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }
        if (isVanished(event.getPlayer())) {
            setTarget(event.getPlayer(), (Player) event.getRightClicked(), false);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtPlayer(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }
        if (isVanished(event.getPlayer())) {
            setTarget(event.getPlayer(), (Player) event.getRightClicked(), false);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (plugin == null || !isStaff(player)) {
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            refreshStaffVisibilityTeam();
            updateVisibility(player);
        });
    }

    private void applyStoredVanishState(Player player) {
        UUID uuid = player.getUniqueId();
        GameMode storedMode = originalGameModes.getOrDefault(uuid, player.getGameMode());
        originalLocations.put(uuid, player.getLocation().clone());
        originalGameModes.put(uuid, storedMode);
        originalFlyingStates.put(uuid, player.isFlying());
        originalAllowFlightStates.put(uuid, player.getAllowFlight());

        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvulnerable(true);
        player.setGameMode(storedMode);

        if (spectatorMode.getOrDefault(uuid, false)) {
            createOrUpdateSpectatorProxy(player);
        }
    }

    private void refreshAllVisibility() {
        if (plugin == null) {
            return;
        }

        for (Player target : Bukkit.getOnlinePlayers()) {
            updateVisibility(target);
        }
    }

    private void applyVisibility(Player viewer, Player target) {
        if (viewer.getUniqueId().equals(target.getUniqueId())) {
            viewer.showPlayer(plugin, target);
            return;
        }

        boolean targetVanished = vanishedPlayers.contains(target.getUniqueId());
        boolean targetSpectatorStaff = spectatorMode.getOrDefault(target.getUniqueId(), false) && isStaff(target);
        if ((targetVanished || targetSpectatorStaff) && !canSeeStealthStaff(viewer)) {
            viewer.hidePlayer(plugin, target);
            return;
        }

        viewer.showPlayer(plugin, target);
    }

    private boolean canSeeStealthStaff(Player viewer) {
        return viewer.hasPermission(STAFF_PERMISSION) || viewer.hasPermission(ADMIN_PERMISSION);
    }

    private boolean isStaff(Player player) {
        return player.hasPermission(STAFF_PERMISSION) || player.hasPermission(ADMIN_PERMISSION);
    }

    public void shutdown() {
        if (spectatorProxyTask != null) {
            spectatorProxyTask.cancel();
        }
        if (staffScoreboardTask != null) {
            staffScoreboardTask.cancel();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            hideStaffModeScoreboard(player);
        }

        for (UUID uuid : new HashSet<>(spectatorProxies.keySet())) {
            removeSpectatorProxy(uuid);
        }
    }

    private void showStaffModeScoreboard(Player player) {
        if (!isVanished(player) || Bukkit.getScoreboardManager() == null) {
            return;
        }

        previousScoreboards.putIfAbsent(player.getUniqueId(), player.getScoreboard());
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(STAFF_MODE_OBJECTIVE, "dummy", ChatColor.GOLD + "Staff Mode");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        player.setScoreboard(scoreboard);
        updateStaffModeScoreboard(player);
    }

    private void hideStaffModeScoreboard(Player player) {
        UUID uuid = player.getUniqueId();
        Scoreboard previous = previousScoreboards.remove(uuid);
        if (previous != null) {
            player.setScoreboard(previous);
        }
    }

    private void refreshStaffModeScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isVanished(player)) {
                updateStaffModeScoreboard(player);
            }
        }
    }

    private void updateStaffModeScoreboard(Player player) {
        if (!isVanished(player)) {
            return;
        }

        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective(STAFF_MODE_OBJECTIVE);
        if (objective == null) {
            showStaffModeScoreboard(player);
            return;
        }

        for (String entry : new HashSet<>(scoreboard.getEntries())) {
            scoreboard.resetScores(entry);
        }

        UUID targetUuid = staffTargets.get(player.getUniqueId());
        Player target = targetUuid != null ? getOnlinePlayer(targetUuid) : null;
        if (target == null && targetUuid != null) {
            staffTargets.remove(player.getUniqueId());
        }

        if (target != null) {
            setLine(objective, 10, ChatColor.YELLOW + "Target: " + ChatColor.WHITE + target.getName());
            setLine(objective, 8, ChatColor.YELLOW + "Ping: " + ChatColor.WHITE + target.getPing() + "ms");
            double hp = Math.max(0.0D, target.getHealth());
            double maxHp = target.getMaxHealth();
            setLine(objective, 6, ChatColor.YELLOW + "TPS: " + ChatColor.WHITE + formatTps());
            setLine(objective, 4, ChatColor.YELLOW + "Health: " + ChatColor.WHITE + formatOneDecimal(hp) + "/" + formatOneDecimal(maxHp) + " hp");
            setLine(objective, 2, ChatColor.YELLOW + "Time: " + ChatColor.WHITE + LocalDateTime.now().format(DATE_TIME_FORMATTER));
            return;
        }

        setLine(objective, 6, ChatColor.YELLOW + "Target: " + ChatColor.WHITE + "None");
        setLine(objective, 4, ChatColor.YELLOW + "TPS: " + ChatColor.WHITE + formatTps());
        setLine(objective, 2, ChatColor.YELLOW + "Time: " + ChatColor.WHITE + LocalDateTime.now().format(DATE_TIME_FORMATTER));
    }

    private void setLine(Objective objective, int scoreValue, String text) {
        String line = text;
        if (line.length() > 40) {
            line = line.substring(0, 40);
        }
        Score score = objective.getScore(line);
        score.setScore(scoreValue);
    }

    private String formatTps() {
        double tps = 20.0D;
        try {
            Object server = Bukkit.getServer();
            if (server != null) {
                java.lang.reflect.Method method = server.getClass().getMethod("getTPS");
                Object value = method.invoke(server);
                if (value instanceof double[] tpsValues && tpsValues.length > 0) {
                    tps = Math.min(20.0D, Math.max(0.0D, tpsValues[0]));
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Keep default TPS fallback for API variants without getTPS.
        }
        return formatOneDecimal(tps);
    }

    private String formatOneDecimal(double value) {
        return String.format(java.util.Locale.US, "%.1f", value);
    }

    private void createOrUpdateSpectatorProxy(Player sourcePlayer) {
        if (plugin == null) {
            return;
        }

        UUID uuid = sourcePlayer.getUniqueId();
        String proxyName = ChatColor.RED + sourcePlayer.getName();
        ArmorStand stand = spectatorProxies.get(uuid);
        Location markerLocation = sourcePlayer.getLocation().clone().add(0.0, 1.8, 0.0);
        if (stand == null || !stand.isValid()) {
            stand = sourcePlayer.getWorld().spawn(markerLocation, ArmorStand.class, spawned -> {
                spawned.setVisible(false);
                spawned.setGravity(false);
                spawned.setMarker(true);
                spawned.setSmall(true);
                spawned.setInvulnerable(true);
                spawned.setCollidable(false);
                spawned.setSilent(true);
                spawned.setCustomName(proxyName);
                spawned.setCustomNameVisible(true);
                spawned.setPersistent(false);
            });

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(sourcePlayer);
                head.setItemMeta(skullMeta);
            }
            if (stand.getEquipment() != null) {
                stand.getEquipment().setHelmet(head);
            }
            spectatorProxies.put(uuid, stand);
        } else {
            stand.setCustomName(proxyName);
            stand.setCustomNameVisible(true);
            stand.teleport(markerLocation);
        }
    }

    private void removeSpectatorProxy(UUID uuid) {
        ArmorStand stand = spectatorProxies.remove(uuid);
        if (stand != null && stand.isValid()) {
            stand.remove();
        }
    }

    private void refreshSpectatorProxies() {
        if (plugin == null) {
            return;
        }

        for (UUID uuid : new HashSet<>(spectatorProxies.keySet())) {
            Player sourcePlayer = getOnlinePlayer(uuid);
            boolean active = sourcePlayer != null
                && sourcePlayer.isOnline()
                && vanishedPlayers.contains(uuid)
                && spectatorMode.getOrDefault(uuid, false)
                && sourcePlayer.getGameMode() == STAFF_OBSERVER_MODE;
            if (!active) {
                removeSpectatorProxy(uuid);
            }
        }

        for (UUID uuid : spectatorMode.keySet()) {
            if (!spectatorMode.getOrDefault(uuid, false)) {
                continue;
            }

            Player sourcePlayer = getOnlinePlayer(uuid);
            if (sourcePlayer == null || !sourcePlayer.isOnline() || !vanishedPlayers.contains(uuid)) {
                continue;
            }
            createOrUpdateSpectatorProxy(sourcePlayer);
        }

        for (Map.Entry<UUID, ArmorStand> entry : spectatorProxies.entrySet()) {
            UUID sourceUuid = entry.getKey();
            ArmorStand stand = entry.getValue();
            Player sourcePlayer = getOnlinePlayer(sourceUuid);
            if (stand == null || !stand.isValid() || sourcePlayer == null || !sourcePlayer.isOnline()) {
                continue;
            }

            stand.teleport(sourcePlayer.getLocation().clone().add(0.0, 1.8, 0.0));
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (canSeeSpectatorProxy(viewer, sourcePlayer)) {
                    viewer.showEntity(plugin, stand);
                } else {
                    viewer.hideEntity(plugin, stand);
                }
            }
        }
    }

    private boolean canSeeSpectatorProxy(Player viewer, Player sourcePlayer) {
        if (!canSeeStealthStaff(viewer)) {
            return false;
        }
        if (viewer.getUniqueId().equals(sourcePlayer.getUniqueId())) {
            return false;
        }
        return viewer.getGameMode() != GameMode.SPECTATOR;
    }

    private void refreshStaffVisibilityTeam() {
        if (plugin == null || Bukkit.getScoreboardManager() == null) {
            return;
        }

        Scoreboard mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = mainBoard.getTeam(STAFF_VISIBILITY_TEAM);
        if (team == null) {
            team = mainBoard.registerNewTeam(STAFF_VISIBILITY_TEAM);
        }
        team.setCanSeeFriendlyInvisibles(true);

        Set<String> staffEntries = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isStaff(player)) {
                staffEntries.add(player.getName());
            }
        }

        for (String entry : new HashSet<>(team.getEntries())) {
            if (!staffEntries.contains(entry)) {
                team.removeEntry(entry);
            }
        }

        for (String entry : staffEntries) {
            if (!team.hasEntry(entry)) {
                team.addEntry(entry);
            }
        }
    }
}
