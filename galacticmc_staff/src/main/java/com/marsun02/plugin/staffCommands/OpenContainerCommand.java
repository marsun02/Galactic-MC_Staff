package com.marsun02.plugin.staffCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class OpenContainerCommand implements CommandExecutor {

    private static final String MOD_PERMISSION = "server.staff.mod";

    private final OfflineInventoryService offlineInventoryService;

    public OpenContainerCommand(OfflineInventoryService offlineInventoryService) {
        this.offlineInventoryService = offlineInventoryService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!player.hasPermission(MOD_PERMISSION)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /" + command.getName() + " <player>");
            return true;
        }

        boolean ender = command.getName().equalsIgnoreCase("openender");
        return offlineInventoryService.openInventoryView(player, args[0], ender);
    }
}