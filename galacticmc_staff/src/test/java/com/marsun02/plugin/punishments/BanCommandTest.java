package com.marsun02.plugin.punishments;

import static org.junit.Assert.assertEquals;

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
}
