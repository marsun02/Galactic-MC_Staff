package com.marsun02.plugin.punishments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.Test;

public class MuteCommandTest {

    @Test
    public void reportsUsageWhenPlayerIsMissing() {
        TestCommandSender sender = new TestCommandSender("mod", true);
        MuteCommand command = new MuteCommand(new HashMap<>(), new HashMap<>(), new HashMap<>());

        boolean result = command.onCommand(sender, new Command("mute") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "mute", new String[0]);

        assertTrue(result);
        assertTrue(sender.getLastMessage().contains("Usage: /mute <player> [amount] [seconds | minutes | hours | days] [reason]"));
    }

    @Test
    public void deniesPermissionToRegularStaff() {
        TestCommandSender sender = new TestCommandSender("mod", false);
        MuteCommand command = new MuteCommand(new HashMap<>(), new HashMap<>(), new HashMap<>());

        boolean result = command.onCommand(sender, new Command("mute") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "mute", new String[] {"player"});

        assertTrue(result);
        assertTrue(sender.getLastMessage().contains("permission"));
    }
}
