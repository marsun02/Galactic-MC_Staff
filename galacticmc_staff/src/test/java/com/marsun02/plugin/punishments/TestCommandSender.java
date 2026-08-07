package com.marsun02.plugin.punishments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class TestCommandSender implements CommandSender {

    private final List<String> messages = new ArrayList<>();
    private final String name;
    private boolean op;
    private final Set<String> permissions;

    public TestCommandSender(String name, boolean op, String... permissions) {
        this.name = name;
        this.op = op;
        this.permissions = java.util.Set.of(permissions);
    }

    public void sendMessage(String message) {
        messages.add(message);
    }

    @Override
    public void sendMessage(String[] messages) {
        for (String message : messages) {
            this.messages.add(message);
        }
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        messages.add(message);
    }

    @Override
    public void sendMessage(UUID uuid, String[] messagesToSend) {
        for (String message : messagesToSend) {
            messages.add(message);
        }
    }

    @Override
    public Server getServer() {
        return null;
    }

    @Override
    public org.bukkit.command.CommandSender.Spigot spigot() {
        return null;
    }

    @Override
    public boolean hasPermission(String permission) {
        return op || permissions.contains(permission);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return hasPermission(permission.getName());
    }

    @Override
    public boolean isPermissionSet(String permission) {
        return op || permissions.contains(permission);
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return isPermissionSet(permission.getName());
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return null;
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
    }

    @Override
    public void recalculatePermissions() {
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return Collections.emptySet();
    }

    @Override
    public boolean isOp() {
        return op;
    }

    @Override
    public void setOp(boolean value) {
        op = value;
    }

    @Override
    public String getName() {
        return name;
    }

    public List<String> getMessages() {
        return messages;
    }

    public String getLastMessage() {
        return messages.isEmpty() ? "" : messages.get(messages.size() - 1);
    }
}
