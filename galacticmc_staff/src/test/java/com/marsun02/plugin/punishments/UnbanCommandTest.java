package com.marsun02.plugin.punishments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.Test;

public class UnbanCommandTest {

    @Test
    public void reportsUsageWhenPlayerIsMissing() {
        TestCommandSender sender = new TestCommandSender("mod", true);
        UnbanCommand command = new UnbanCommand(new HashMap<>());

        boolean result = command.onCommand(sender, new Command("unban") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "unban", new String[0]);

        assertTrue(result);
        assertTrue(sender.getLastMessage().contains("Usage: /unban <player>"));
    }

    @Test
    public void deniesPermissionToRegularStaff() {
        TestCommandSender sender = new TestCommandSender("mod", false);
        UnbanCommand command = new UnbanCommand(new HashMap<>());

        boolean result = command.onCommand(sender, new Command("unban") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "unban", new String[] {"player"});

        assertTrue(result);
        assertTrue(sender.getLastMessage().contains("permission"));
    }
}
