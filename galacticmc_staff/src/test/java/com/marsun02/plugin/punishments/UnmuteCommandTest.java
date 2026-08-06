package com.marsun02.plugin.punishments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.junit.Test;

public class UnmuteCommandTest {

    @Test
    public void showsUsageWhenNoTargetIsProvided() {
        List<String> messages = new ArrayList<>();
        CommandSender sender = PunishmentTestSupport.createSender(messages);
        UnmuteCommand command = new UnmuteCommand(new HashMap<>(), new HashMap<>(), new HashMap<>());

        boolean handled = command.onCommand(sender, null, "unmute", new String[0]);

        assertTrue(handled);
        assertEquals(1, messages.size());
        assertTrue(messages.get(0).contains("Usage: /unmute"));
    }
}
