package com.crayonsmp.neomUtilities.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final Pattern START_WITH_COLOR_PATTERN = Pattern.compile(
            "^(?:[&§][0-9a-fk-or]|#[a-fA-F0-9]{6}).*",
            Pattern.CASE_INSENSITIVE
    );

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(format(message));
    }

    public static String format(String message) {
        Matcher matcher = START_WITH_COLOR_PATTERN.matcher(message);

        if (!matcher.matches()) {
            message = "&7" + message;
        }

        String translatedMessage = ChatColor.translateAlternateColorCodes('&', message);

        return hex(translatedMessage);
    }

    public static String alternateColor(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static Component miniMessage(String message, TagResolver... tagResolvers) {
        return MM.deserialize(alternateColor(message), tagResolvers);
    }

    public static String hex(String message) {
        Pattern pattern = Pattern.compile("<#[a-fA-F0-9]{6}>");
        Matcher matcher = pattern.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String fullHexCode = matcher.group();
            String hexCode = fullHexCode.substring(1, fullHexCode.length() - 1);

            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            matcher.appendReplacement(result, builder.toString());
        }

        matcher.appendTail(result);
        return ChatColor.translateAlternateColorCodes('&', result.toString());
    }
}