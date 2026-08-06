package com.marsun02.plugin.punishments;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

public final class PunishmentTestSupport {

    private PunishmentTestSupport() {
    }

    public static CommandSender createSender() {
        return createSender(new ArrayList<>());
    }

    public static CommandSender createSender(List<String> messages) {
        return (CommandSender) Proxy.newProxyInstance(
                CommandSender.class.getClassLoader(),
                new Class<?>[]{CommandSender.class},
                new CapturingHandler(messages));
    }

    private static final class CapturingHandler implements InvocationHandler {
        private final List<String> messages;

        private CapturingHandler(List<String> messages) {
            this.messages = messages;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if ("sendMessage".equals(name)) {
                if (args != null && args.length > 0) {
                    if (args[0] instanceof String) {
                        messages.add((String) args[0]);
                    } else if (args[0] instanceof String[]) {
                        for (String message : (String[]) args[0]) {
                            messages.add(message);
                        }
                    }
                }
                return null;
            }
            if ("hasPermission".equals(name)) {
                return true;
            }
            if ("getName".equals(name)) {
                return "TestSender";
            }
            if ("isOp".equals(name)) {
                return false;
            }
            if ("setOp".equals(name)) {
                return null;
            }
            if ("isPermissionSet".equals(name)) {
                return true;
            }

            Class<?> returnType = method.getReturnType();
            if (returnType == boolean.class) return false;
            if (returnType == int.class) return 0;
            if (returnType == long.class) return 0L;
            if (returnType == double.class) return 0D;
            if (returnType == float.class) return 0F;
            if (returnType == short.class) return (short) 0;
            if (returnType == byte.class) return (byte) 0;
            if (returnType == char.class) return '\0';
            return null;
        }
    }
}
