package com.marsun02.plugin.punishments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.Test;

public class HistoryClearCommandTest {

    @Test
    public void reportsUsageWhenTargetIsMissing() {
        TestCommandSender sender = new TestCommandSender("admin", true);
        Map<String, List<HistoryCommand.PunishmentRecord>> history = new HashMap<>();
        HistoryClearCommand command = new HistoryClearCommand(history, null, new HashMap<>(), new HashMap<>());

        boolean result = command.onCommand(sender, new Command("history") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "history", new String[0]);

        assertTrue(result);
        assertTrue(sender.getLastMessage().contains("Usage: /history clear <player> [amount]"));
    }

    @Test
    public void clearsRecentEntriesAndRemovesWarnState() {
        TestCommandSender sender = new TestCommandSender("admin", true);
        Map<String, List<HistoryCommand.PunishmentRecord>> history = new HashMap<>();
        List<HistoryCommand.PunishmentRecord> entries = new ArrayList<>();
        entries.add(new HistoryCommand.PunishmentRecord("banned", "mod", "bad", 3L, 4L, true));
        entries.add(new HistoryCommand.PunishmentRecord("warned", "mod", "spam", 1L, 2L, true));
        history.put("player", entries);

        Map<String, Long> warnedPlayers = new HashMap<>();
        warnedPlayers.put("player", 5L);
        Map<String, String> warnedReasons = new HashMap<>();
        warnedReasons.put("player", "spam");

        HistoryClearCommand command = new HistoryClearCommand(history, null, warnedPlayers, warnedReasons);
        boolean result = command.onCommand(sender, new Command("history") {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                return true;
            }
        }, "history", new String[] {"player", "1"});

        assertTrue(result);
        assertEquals(1, history.get("player").size());
        assertTrue(warnedPlayers.isEmpty());
        assertTrue(warnedReasons.isEmpty());
    }
}
