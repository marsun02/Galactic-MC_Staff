package com.marsun02.plugin;

import java.util.ArrayList;
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
import com.marsun02.plugin.punishments.HistoryClearCommand;
import com.marsun02.plugin.punishments.HistoryCommand;
import com.marsun02.plugin.punishments.KickCommand;
import com.marsun02.plugin.punishments.MuteCommand;
import com.marsun02.plugin.punishments.UnbanCommand;
import com.marsun02.plugin.punishments.UnmuteCommand;
import com.marsun02.plugin.punishments.WarnCommand;
import com.marsun02.plugin.punishments.WarnListener;
import com.marsun02.plugin.staffChat.StaffChat;
import com.marsun02.plugin.staffChat.StaffChatListener;
import com.marsun02.plugin.staffCommands.OfflineInventoryService;
import com.marsun02.plugin.staffCommands.OnlineStaff;
import com.marsun02.plugin.staffCommands.OpenContainerCommand;
import com.marsun02.plugin.staffCommands.ReportCommand;
import com.marsun02.plugin.staffCommands.ReportTeleportCommand;
import com.marsun02.plugin.staffMode.StaffTeleportCommand;
import com.marsun02.plugin.staffMode.VanishCommand;
import com.marsun02.plugin.staffMode.VanishManager;
import com.marsun02.plugin.staffMode.VanishToggleListener;

public class Main extends JavaPlugin {
    private final Set<UUID> staffChatToggled = new HashSet<>();
    private final Map<String, Long> mutedPlayers = new HashMap<>();
    private final Map<String, String> mutedReasons = new HashMap<>();
    private final Map<String, Long> warnedPlayers = new HashMap<>();
    private final Map<String, String> warnedReasons = new HashMap<>();
    private final Map<String, List<HistoryCommand.PunishmentRecord>> punishmentsHistory = new HashMap<>();
    private VanishManager vanishManager;
    
    @Override
    public void onEnable(){
        // Load config
        saveDefaultConfig();
        loadStaffChatStates();
        loadMutedPlayers();
        loadWarnedPlayers();
        loadPunishmentHistory();

        vanishManager = new VanishManager(this);
        vanishManager.loadState(getConfig());
        OfflineInventoryService offlineInventoryService = new OfflineInventoryService(this);

        // Register staff commands
        getCommand("staff").setExecutor(new OnlineStaff());
        getCommand("staffchat").setExecutor(new StaffChat(staffChatToggled));
        getCommand("vanish").setExecutor(new VanishCommand(vanishManager));
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("reporttp").setExecutor(new ReportTeleportCommand());
        OpenContainerCommand openContainerCommand = new OpenContainerCommand(offlineInventoryService);
        getCommand("openinv").setExecutor(openContainerCommand);
        getCommand("openender").setExecutor(openContainerCommand);
        StaffTeleportCommand staffTeleportCommand = new StaffTeleportCommand(vanishManager);
        getCommand("tpo").setExecutor(staffTeleportCommand);
        getCommand("tpohere").setExecutor(staffTeleportCommand);
        getCommand("tppos").setExecutor(staffTeleportCommand);
        
        // Register punishment commands
        getCommand("kick").setExecutor(new KickCommand(punishmentsHistory));
        getCommand("ban").setExecutor(new BanCommand(punishmentsHistory));
        getCommand("unban").setExecutor(new UnbanCommand(punishmentsHistory));
        getCommand("mute").setExecutor(new MuteCommand(mutedPlayers, mutedReasons, punishmentsHistory));
        getCommand("unmute").setExecutor(new UnmuteCommand(mutedPlayers, mutedReasons, punishmentsHistory));
        getCommand("warn").setExecutor(new WarnCommand(warnedPlayers, warnedReasons, punishmentsHistory));
        HistoryClearCommand clearCommand = new HistoryClearCommand(punishmentsHistory, this, warnedPlayers, warnedReasons);
        getCommand("history").setExecutor(new HistoryCommand(punishmentsHistory, clearCommand));
        
        // Register StaffChat listener
        getServer().getPluginManager().registerEvents(new StaffChatListener(staffChatToggled, mutedPlayers, mutedReasons), this);
        // Register warn listener
        getServer().getPluginManager().registerEvents(new WarnListener(warnedPlayers, warnedReasons, this), this);
        getServer().getPluginManager().registerEvents(vanishManager, this);
        getServer().getPluginManager().registerEvents(new VanishToggleListener(vanishManager), this);
        getServer().getPluginManager().registerEvents(offlineInventoryService, this);

        getLogger().info("Galactic-MC_Staff er aktivert");
    }

    @Override
    public void onDisable() {
        saveStaffChatStates();
        saveMutedPlayers();
        saveWarnedPlayers();
        savePunishmentHistory();
        if (vanishManager != null) {
            vanishManager.saveState(getConfig());
            vanishManager.shutdown();
        }
        saveConfig();
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

    private void loadPunishmentHistory() {
        FileConfiguration config = getConfig();
        ConfigurationSection historySection = config.getConfigurationSection("history");
        if (historySection == null) return;

        for (String playerName : historySection.getKeys(false)) {
            ConfigurationSection playerSection = historySection.getConfigurationSection(playerName);
            if (playerSection == null) continue;

            List<HistoryCommand.PunishmentRecord> records = new ArrayList<>();
            for (String recordKey : playerSection.getKeys(false)) {
                ConfigurationSection recordSection = playerSection.getConfigurationSection(recordKey);
                if (recordSection == null) continue;

                String type = recordSection.getString("type", "unknown");
                String actor = recordSection.getString("actor", "Console");
                String reason = recordSection.getString("reason", "No reason provided.");
                long createdAt = recordSection.getLong("createdAt", System.currentTimeMillis());
                Long expiresAt = recordSection.isSet("expiresAt") ? recordSection.getLong("expiresAt") : null;
                boolean active = recordSection.getBoolean("active", false);

                records.add(new HistoryCommand.PunishmentRecord(type, actor, reason, createdAt, expiresAt, active));
            }

            punishmentsHistory.put(playerName.toLowerCase(), records);
        }
    }

    public void savePunishmentHistory() {
        FileConfiguration config = getConfig();
        config.set("history", null);

        for (var entry : punishmentsHistory.entrySet()) {
            String playerName = entry.getKey();
            List<HistoryCommand.PunishmentRecord> records = entry.getValue();
            for (int i = 0; i < records.size(); i++) {
                HistoryCommand.PunishmentRecord record = records.get(i);
                config.set("history." + playerName + "." + i + ".type", record.getType());
                config.set("history." + playerName + "." + i + ".actor", record.getActor());
                config.set("history." + playerName + "." + i + ".reason", record.getReason());
                config.set("history." + playerName + "." + i + ".createdAt", record.getCreatedAt());
                if (record.getExpiresAt() != null) {
                    config.set("history." + playerName + "." + i + ".expiresAt", record.getExpiresAt());
                }
                config.set("history." + playerName + "." + i + ".active", record.isActive());
            }
        }

        saveConfig();
    }

}
