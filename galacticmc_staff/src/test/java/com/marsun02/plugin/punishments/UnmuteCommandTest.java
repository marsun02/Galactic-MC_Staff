package com.marsun02.plugin.punishments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.Test;

public class UnmuteCommandTest {

    @Test
    public void reportsUsageWhenPlayerIsMissing() {
        TestCommandSender sender = new TestCommandSender("mod", true);
        UnmuteCommand command = new UnmuteCommand(new HashMap<>(), new HashMap<>(), new HashMap<>());

        boolean result = command.onCommand(sender, new Command("unmute") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "unmute", new String[0]);

        assertTrue(result);
        assertTrue(sender.getLastMessage().contains("Usage: /unmute <player>"));
    }

    @Test
    public void deniesPermissionToRegularStaff() {
        TestCommandSender sender = new TestCommandSender("mod", false);
        UnmuteCommand command = new UnmuteCommand(new HashMap<>(), new HashMap<>(), new HashMap<>());

        boolean result = command.onCommand(sender, new Command("unmute") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "unmute", new String[] {"player"});

        assertTrue(result);
        assertTrue(sender.getLastMessage().contains("permission"));
    }
}
