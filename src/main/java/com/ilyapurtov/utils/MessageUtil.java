package com.ilyapurtov.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public class MessageUtil {

    public static FileConfiguration messages;

    public static void send(CommandSender sender, String configName) {

        if (messages == null) throw new NullPointerException("Messages not initialized");

        String message = messages.getString(configName);
        if (message != null) {
            String prefix = messages.getString("prefix") == null ? "" : messages.getString("prefix");
            sender.sendMessage((prefix + message).replace("&", "\u00a7"));
        }
    }

    public static void send(CommandSender sender, String configName, Map<String, Object> placeholders) {

        if (messages == null) throw new NullPointerException("Messages not initialized");

        String message = messages.getString(configName);

        if (message != null) {
            for (Map.Entry<String, Object> placeholder : placeholders.entrySet()) {
                message = message.replace(placeholder.getKey(), String.valueOf(placeholder.getValue()));
            }
            String prefix = messages.getString("prefix") == null ? "" : messages.getString("prefix");
            sender.sendMessage((prefix + message).replace("&", "\u00a7"));
        }
    }

    public static void sendChannelNoPermission(CommandSender sender, String configName) {
        if (messages == null) throw new NullPointerException("Messages not initialized");

        String message = messages.getString("channelNoPermission." + configName);
        if (message != null) {
            String prefix = messages.getString("prefix") == null ? "" : messages.getString("prefix");
            sender.sendMessage((prefix + message).replace("&", "\u00a7"));
        } else {
            send(sender, "noPermission");
        }
    }

}
