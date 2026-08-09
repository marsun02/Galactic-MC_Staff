package com.marsun02.plugin.staffMode;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class VanishCommand implements CommandExecutor {

    private static final String STAFF_PERMISSION = "server.staff.staffmember";
    private static final String ADMIN_PERMISSION = "server.staff.admin";
    private final VanishManager vanishManager;

    public VanishCommand(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Usage: /vanish <player>");
                return true;
            }

            if (!player.hasPermission(STAFF_PERMISSION) && !player.hasPermission(ADMIN_PERMISSION)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            vanishManager.toggleVanish(player);
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /vanish or /vanish <player>");
            return true;
        }

        if (!sender.hasPermission(ADMIN_PERMISSION) && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to manage another player's vanish state.");
            return true;
        }

        String targetName = args[0];
        boolean changed = vanishManager.toggleVanish(targetName);
        if (changed) {
            sender.sendMessage(ChatColor.GREEN + "Toggled vanish for " + targetName + ".");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "No vanish state change was made for " + targetName + ".");
        }
        return true;
    }
}
