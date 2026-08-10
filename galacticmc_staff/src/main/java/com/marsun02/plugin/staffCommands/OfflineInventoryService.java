package com.marsun02.plugin.staffCommands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class OfflineInventoryService implements Listener {

    private static final String CACHE_PATH = "offline-cache";
    private static final String PENDING_PATH = "offline-pending";

    private final JavaPlugin plugin;

    public OfflineInventoryService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean openInventoryView(Player staff, String targetName, boolean enderChest) {
        Player onlineTarget = Bukkit.getPlayerExact(targetName);
        if (onlineTarget != null) {
            Inventory inv = enderChest ? onlineTarget.getEnderChest() : onlineTarget.getInventory();
            staff.openInventory(inv);
            staff.sendMessage(ChatColor.GREEN + "Opened " + onlineTarget.getName() + "'s " + (enderChest ? "ender chest" : "inventory") + ".");
            return true;
        }

        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
        if (offlineTarget == null || (!offlineTarget.hasPlayedBefore() && offlineTarget.getName() == null)) {
            staff.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        UUID uuid = offlineTarget.getUniqueId();
        String displayName = offlineTarget.getName() != null ? offlineTarget.getName() : targetName;
        Inventory inventory = createOfflineInventory(uuid, displayName, enderChest);

        ItemStack[] stored = getStoredContents(uuid, enderChest);
        inventory.setContents(stored);
        staff.openInventory(inventory);
        staff.sendMessage(ChatColor.YELLOW + "Opened offline " + displayName + "'s " + (enderChest ? "ender chest" : "inventory") + ".");
        return true;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        saveCachedContents(player.getUniqueId(), false, copy(player.getInventory().getContents()));
        saveCachedContents(player.getUniqueId(), true, copy(player.getEnderChest().getContents()));
        plugin.saveConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        ItemStack[] pendingInventory = getPendingContents(uuid, false);
        if (pendingInventory != null) {
            player.getInventory().setContents(pendingInventory);
            clearPending(uuid, false);
            player.sendMessage(ChatColor.YELLOW + "Your inventory was updated by staff moderation while you were offline.");
        }

        ItemStack[] pendingEnder = getPendingContents(uuid, true);
        if (pendingEnder != null) {
            player.getEnderChest().setContents(pendingEnder);
            clearPending(uuid, true);
            player.sendMessage(ChatColor.YELLOW + "Your ender chest was updated by staff moderation while you were offline.");
        }

        saveCachedContents(uuid, false, copy(player.getInventory().getContents()));
        saveCachedContents(uuid, true, copy(player.getEnderChest().getContents()));
        plugin.saveConfig();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder() instanceof OfflineInventoryHolder holder)) {
            return;
        }

        ItemStack[] contents = copy(topInventory.getContents());
        saveCachedContents(holder.uuid(), holder.enderChest(), contents);
        savePendingContents(holder.uuid(), holder.enderChest(), contents);
        plugin.saveConfig();
    }

    private Inventory createOfflineInventory(UUID uuid, String name, boolean enderChest) {
        OfflineInventoryHolder holder = new OfflineInventoryHolder(uuid, enderChest);
        if (enderChest) {
            return Bukkit.createInventory(holder, 27, ChatColor.DARK_PURPLE + "Offline Ender: " + name);
        }
        return Bukkit.createInventory(holder, 54, ChatColor.GOLD + "Offline Inv: " + name);
    }

    private ItemStack[] getStoredContents(UUID uuid, boolean enderChest) {
        ItemStack[] pending = getPendingContents(uuid, enderChest);
        if (pending != null) {
            return pending;
        }

        List<?> rawList = plugin.getConfig().getList(path(CACHE_PATH, uuid, enderChest));
        if (rawList == null) {
            return enderChest ? new ItemStack[27] : new ItemStack[54];
        }

        int size = enderChest ? 27 : 54;
        ItemStack[] contents = new ItemStack[size];
        for (int i = 0; i < Math.min(rawList.size(), size); i++) {
            Object raw = rawList.get(i);
            if (raw instanceof ItemStack stack) {
                contents[i] = stack;
            }
        }
        return contents;
    }

    private ItemStack[] getPendingContents(UUID uuid, boolean enderChest) {
        List<?> rawList = plugin.getConfig().getList(path(PENDING_PATH, uuid, enderChest));
        if (rawList == null) {
            return null;
        }

        int size = enderChest ? 27 : 54;
        ItemStack[] contents = new ItemStack[size];
        for (int i = 0; i < Math.min(rawList.size(), size); i++) {
            Object raw = rawList.get(i);
            if (raw instanceof ItemStack stack) {
                contents[i] = stack;
            }
        }
        return contents;
    }

    private void saveCachedContents(UUID uuid, boolean enderChest, ItemStack[] contents) {
        plugin.getConfig().set(path(CACHE_PATH, uuid, enderChest), toList(contents));
    }

    private void savePendingContents(UUID uuid, boolean enderChest, ItemStack[] contents) {
        plugin.getConfig().set(path(PENDING_PATH, uuid, enderChest), toList(contents));
    }

    private void clearPending(UUID uuid, boolean enderChest) {
        plugin.getConfig().set(path(PENDING_PATH, uuid, enderChest), null);
    }

    private String path(String root, UUID uuid, boolean enderChest) {
        return root + "." + uuid + "." + (enderChest ? "ender" : "inventory");
    }

    private List<ItemStack> toList(ItemStack[] contents) {
        List<ItemStack> list = new ArrayList<>(contents.length);
        for (ItemStack item : contents) {
            list.add(item);
        }
        return list;
    }

    private ItemStack[] copy(ItemStack[] contents) {
        ItemStack[] copied = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            copied[i] = contents[i] == null ? null : contents[i].clone();
        }
        return copied;
    }

    private record OfflineInventoryHolder(UUID uuid, boolean enderChest) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}