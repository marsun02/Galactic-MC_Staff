package com.marsun02.plugin.punishments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.Test;

public class BanCommandTest {

    @Test
    public void parsesSeparatedDurationValues() {
        assertEquals(300L, BanCommand.parseDurationSeconds("5", "minutes"));
        assertEquals(86400L, BanCommand.parseDurationSeconds("1", "day"));
    }

    @Test
    public void parsesCompactDurationTokens() {
        assertEquals(30L, BanCommand.parseDurationSecondsToken("30s"));
        assertEquals(7200L, BanCommand.parseDurationSecondsToken("2h"));
    }

    @Test
    public void reportsUsageWhenPlayerIsMissing() {
        TestCommandSender sender = new TestCommandSender("mod", true, "server.staff.mod");
        BanCommand command = new BanCommand(new HashMap<>());

        boolean result = command.onCommand(sender, new Command("ban") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "ban", new String[0]);

        assertTrue(result);
        assertTrue(sender.getLastMessage().contains("Usage: /ban <player> [amount] [seconds | minutes | hours | days] [reason]"));
    }

    @Test
    public void parsesZeroDurationAsPermanent() {
        assertEquals(0L, BanCommand.parseDurationSecondsToken("0s"));
    }

    @Test
    public void handlesLongDurationsAndRoleLimitBoundary() {
        assertEquals(2_592_000L, BanCommand.parseDurationSeconds("30", "day"));
        assertTrue(BanCommand.isDurationToken("31d"));
    }

    @Test
    public void recognizesSupportedDurationUnits() {
        assertTrue(BanCommand.isUnit("seconds"));
        assertTrue(BanCommand.isUnit("hours"));
        assertTrue(BanCommand.isDurationToken("2h"));
    }

    @Test
    public void rejectsUnsupportedDurationTokens() {
        assertTrue(!BanCommand.isDurationToken("2weeks"));
        assertTrue(!BanCommand.isUnit("week"));
    }

    @Test
    public void parseDurationSecondsThrowsForUnsupportedUnit() {
        try {
            BanCommand.parseDurationSeconds("1", "week");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Unsupported unit"));
        }
    }
}
