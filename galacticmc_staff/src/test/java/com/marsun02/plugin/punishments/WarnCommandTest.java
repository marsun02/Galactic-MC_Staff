package com.marsun02.plugin.punishments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.Test;

public class WarnCommandTest {

    @Test
    public void reportsUsageWhenReasonIsMissing() {
        TestCommandSender sender = new TestCommandSender("mod", true);
        WarnCommand command = new WarnCommand(new HashMap<>(), new HashMap<>(), new HashMap<>());

        boolean result = command.onCommand(sender, new Command("warn") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "warn", new String[] {"player"});

        assertTrue(result);
        assertTrue(sender.getLastMessage().contains("Usage: /warn <player> <reason>"));
    }

    @Test
    public void deniesPermissionToRegularStaff() {
        TestCommandSender sender = new TestCommandSender("mod", false);
        WarnCommand command = new WarnCommand(new HashMap<>(), new HashMap<>(), new HashMap<>());

        boolean result = command.onCommand(sender, new Command("warn") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "warn", new String[] {"player", "spam"});

        assertTrue(result);
        assertTrue(sender.getLastMessage().contains("permission"));
    }
}
