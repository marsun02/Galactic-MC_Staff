package com.marsun02.plugin.punishments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        assertEquals("0 days, 0 hours, 5 minutes", HistoryCommand.formatDuration(300_000L));
        assertEquals("1 day, 0 hours, 3 minutes", HistoryCommand.formatDuration(86_583_000L));
        assertEquals("1 day, 2 hours, 3 minutes", HistoryCommand.formatDuration(93_780_000L));
    }
}
