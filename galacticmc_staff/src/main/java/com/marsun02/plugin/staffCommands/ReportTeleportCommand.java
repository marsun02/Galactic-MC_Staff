package com.marsun02.plugin.staffCommands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class ReportTeleportCommand implements CommandExecutor {

    private static final String STAFF_PERMISSION = "server.staff.staffmember";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!player.hasPermission(STAFF_PERMISSION)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 4) {
            player.sendMessage(ChatColor.RED + "Usage: /reporttp <world> <x> <y> <z>");
            return true;
        }

        World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "World not found: " + args[0]);
            return true;
        }

        try {
            double x = Double.parseDouble(args[1]);
            double y = Double.parseDouble(args[2]);
            double z = Double.parseDouble(args[3]);
            Location destination = new Location(world, x + 0.5D, y, z + 0.5D, player.getLocation().getYaw(), player.getLocation().getPitch());
            player.teleport(destination);
            player.sendMessage(ChatColor.GREEN + "Teleported to report location.");
        } catch (NumberFormatException exception) {
            player.sendMessage(ChatColor.RED + "Coordinates must be valid numbers.");
        }
        return true;
    }
}