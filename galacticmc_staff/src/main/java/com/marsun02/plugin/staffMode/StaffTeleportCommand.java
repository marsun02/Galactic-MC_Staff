package com.marsun02.plugin.staffMode;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class StaffTeleportCommand implements CommandExecutor {

    private static final String STAFF_PERMISSION = "server.staff.staffmember";

    private final VanishManager vanishManager;

    public StaffTeleportCommand(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!hasStaffAccess(player)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (!vanishManager.isVanished(player)) {
            player.sendMessage(ChatColor.RED + "You must be in staff mode to use this command.");
            return true;
        }

        String commandName = command.getName().toLowerCase();
        return switch (commandName) {
            case "tpo" -> handleTeleportToPlayer(player, args);
            case "tpohere" -> handleTeleportPlayerHere(player, args);
            case "tppos" -> handleTeleportToPosition(player, args);
            default -> false;
        };
    }

    private boolean handleTeleportToPlayer(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /tpo <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        player.teleport(target.getLocation());
        player.sendMessage(ChatColor.GREEN + "Teleported to " + target.getName() + ".");
        return true;
    }

    private boolean handleTeleportPlayerHere(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /tpohere <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        target.teleport(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Teleported " + target.getName() + " to you.");
        return true;
    }

    private boolean handleTeleportToPosition(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + "Usage: /tppos <x> <y> <z>");
            return true;
        }

        try {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);
            World world = player.getWorld();
            Location destination = new Location(world, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());
            player.teleport(destination);
            player.sendMessage(ChatColor.GREEN + "Teleported to coordinates: " + x + ", " + y + ", " + z + ".");
        } catch (NumberFormatException exception) {
            player.sendMessage(ChatColor.RED + "Coordinates must be valid numbers.");
        }
        return true;
    }

    private boolean hasStaffAccess(Player player) {
        return player.hasPermission(STAFF_PERMISSION);
    }
}