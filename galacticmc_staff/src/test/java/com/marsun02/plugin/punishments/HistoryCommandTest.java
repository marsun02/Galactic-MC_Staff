package com.marsun02.plugin.punishments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.Test;

public class HistoryCommandTest {

    @Test
    public void unbanAndUnmuteShouldRemainVisibleButNotCountAsMeaningfulPunishments() {
        assertTrue(HistoryCommand.shouldDisplayInHistory("unban"));
        assertTrue(HistoryCommand.shouldDisplayInHistory("unmute"));
        assertFalse(HistoryCommand.shouldDisplayInHistory("ban") == false);
    }

    @Test
    public void durationFormattingShouldUseDaysHoursAndMinutes() {
        assertEquals("5 minutes", HistoryCommand.formatDuration(300_000L));
        assertEquals("1 day, 3 minutes, 3 seconds", HistoryCommand.formatDuration(86_583_000L));
        assertEquals("1 day, 2 hours, 3 minutes", HistoryCommand.formatDuration(93_780_000L));
    }

    @Test
    public void reportsUsageWhenNoTargetIsProvided() {
        TestCommandSender sender = new TestCommandSender("mod", true, "server.staff.staffmember");
        Map<String, List<HistoryCommand.PunishmentRecord>> history = new HashMap<>();
        HistoryCommand command = new HistoryCommand(history);

        boolean result = command.onCommand(sender, new Command("history") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "history", new String[0]);

        assertTrue(result);
        assertTrue(sender.getLastMessage().contains("Usage: /history <player> [page]"));
    }

    @Test
    public void deniesAccessToUsersWithoutStaffPermission() {
        TestCommandSender sender = new TestCommandSender("player", false);
        HistoryCommand command = new HistoryCommand(new HashMap<>());

        boolean result = command.onCommand(sender, new Command("history") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "history", new String[] {"Alice"});

        assertTrue(result);
        assertTrue(sender.getLastMessage().contains("You do not have permission"));
    }

    @Test
    public void showsEmptyHistoryMessageForMissingPlayer() {
        TestCommandSender sender = new TestCommandSender("mod", true, "server.staff.staffmember");
        Map<String, List<HistoryCommand.PunishmentRecord>> history = new HashMap<>();
        HistoryCommand command = new HistoryCommand(history);

        boolean result = command.onCommand(sender, new Command("history") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "history", new String[] {"Alice"});

        assertTrue(result);
        assertTrue(sender.getLastMessage().contains("No punishment history found for Alice."));
    }

    @Test
    public void rendersHistoryEntriesWithReasonStatusAndExpiry() {
        TestCommandSender sender = new TestCommandSender("mod", true, "server.staff.staffmember");
        Map<String, List<HistoryCommand.PunishmentRecord>> history = new HashMap<>();
        List<HistoryCommand.PunishmentRecord> records = new ArrayList<>();
        long now = System.currentTimeMillis();
        records.add(new HistoryCommand.PunishmentRecord("banned", "mod", "cheating", now - 5_000, now + 30_000, true));
        history.put("alice", records);
        HistoryCommand command = new HistoryCommand(history);

        boolean result = command.onCommand(sender, new Command("history") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "history", new String[] {"Alice"});

        assertTrue(result);
        assertTrue(sender.getMessages().stream().anyMatch(message -> message.contains("History for Alice")));
        assertTrue(sender.getMessages().stream().anyMatch(message -> message.contains("Reason:")));
        assertTrue(sender.getMessages().stream().anyMatch(message -> message.contains("Status:")));
        assertTrue(sender.getMessages().stream().anyMatch(message -> message.contains("Expires in:")));
    }

    @Test
    public void routesClearSubcommandToClearHandler() {
        TestCommandSender sender = new TestCommandSender("admin", true, "server.staff.admin");
        Map<String, List<HistoryCommand.PunishmentRecord>> history = new HashMap<>();
        HistoryCommand command = new HistoryCommand(history, new HistoryClearCommand(history, null, new HashMap<>(), new HashMap<>()) {
            @Override
            public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
                commandSender.sendMessage("clear-handler-ran");
                return true;
            }
        });

        boolean result = command.onCommand(sender, new Command("history") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "history", new String[] {"clear", "Alice"});

        assertTrue(result);
        assertTrue(sender.getMessages().contains("clear-handler-ran"));
    }
}
