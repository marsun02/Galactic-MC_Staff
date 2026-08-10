package com.marsun02.plugin.staffCommands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class ReportCommand implements CommandExecutor {

    private static final String STAFF_PERMISSION = "server.staff.staffmember";

    private final JavaPlugin plugin;

    public ReportCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player reporter)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length < 2) {
            reporter.sendMessage(ChatColor.RED + "Usage: /report <player> <reason>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            reporter.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        Location location = reporter.getLocation();
        World world = location.getWorld();
        if (world == null) {
            reporter.sendMessage(ChatColor.RED + "Could not determine report world.");
            return true;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        String worldName = world.getName();

        reporter.sendMessage(ChatColor.GREEN + "Report sent for " + target.getName() + ".");

        String title = ChatColor.RED + "Player Reported";
        String subtitle = ChatColor.YELLOW + reporter.getName() + ChatColor.GRAY + " reported " + ChatColor.YELLOW + target.getName();
        String chatPrefix = ChatColor.RED + "[Report] " + ChatColor.YELLOW + reporter.getName() + ChatColor.GRAY + " -> "
            + ChatColor.YELLOW + target.getName() + ChatColor.GRAY + ": " + ChatColor.WHITE + reason;

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (!canViewReports(staff)) {
                continue;
            }

            staff.sendTitle(title, subtitle, 5, 70, 10);
            staff.sendMessage(chatPrefix);

            TextComponent worldInfo = new TextComponent(ChatColor.GOLD + "World: ");
            TextComponent teleportLink = new TextComponent(ChatColor.AQUA + worldName + " [" + x + ", " + y + ", " + z + "]");
            teleportLink.setUnderlined(true);
            teleportLink.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reporttp " + worldName + " " + x + " " + y + " " + z));
            teleportLink.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to teleport to report location")));
            worldInfo.addExtra(teleportLink);
            staff.spigot().sendMessage(worldInfo);
        }

        plugin.getLogger().info("[Report] " + reporter.getName() + " reported " + target.getName() + " in " + worldName
            + " (" + x + ", " + y + ", " + z + ") for: " + reason);
        return true;
    }

    private boolean canViewReports(Player player) {
        return player.hasPermission(STAFF_PERMISSION);
    }
}