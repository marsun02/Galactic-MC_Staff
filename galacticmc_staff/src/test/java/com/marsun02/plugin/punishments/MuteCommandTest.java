package com.marsun02.plugin.punishments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.junit.Test;

public class MuteCommandTest {

    @Test
    public void showsUsageWhenNoArgumentsAreProvided() {
        List<String> messages = new ArrayList<>();
        CommandSender sender = PunishmentTestSupport.createSender(messages);
        MuteCommand command = new MuteCommand(new HashMap<>(), new HashMap<>(), new HashMap<>());

        boolean handled = command.onCommand(sender, null, "mute", new String[0]);

        assertTrue(handled);
        assertEquals(1, messages.size());
        assertTrue(messages.get(0).contains("Usage: /mute"));
    }

    @Test
    public void formatsDurationWithReadableUnits() throws Exception {
        MuteCommand command = new MuteCommand(new HashMap<>(), new HashMap<>(), new HashMap<>());
        Method method = MuteCommand.class.getDeclaredMethod("formatDuration", long.class);
        method.setAccessible(true);

        assertEquals("1 hour(s)", method.invoke(command, 3_600L));
        assertEquals("2 minute(s)", method.invoke(command, 120L));
    }
}
