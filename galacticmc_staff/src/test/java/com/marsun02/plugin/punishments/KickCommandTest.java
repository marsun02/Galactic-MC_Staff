package com.marsun02.plugin.punishments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.Test;

public class KickCommandTest {

    @Test
    public void reportsUsageWhenPlayerIsMissing() {
        TestCommandSender sender = new TestCommandSender("mod", true);
        KickCommand command = new KickCommand(new HashMap<>());

        boolean result = command.onCommand(sender, new Command("kick") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "kick", new String[0]);

        assertTrue(result);
        assertTrue(sender.getLastMessage().contains("Usage: /kick <player> [reason]"));
    }

    @Test
    public void deniesPermissionToRegularStaff() {
        TestCommandSender sender = new TestCommandSender("mod", false);
        KickCommand command = new KickCommand(new HashMap<>());

        boolean result = command.onCommand(sender, new Command("kick") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "kick", new String[] {"player"});

        assertTrue(result);
        assertTrue(sender.getLastMessage().contains("permission"));
    }
}
