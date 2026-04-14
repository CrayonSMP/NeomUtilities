package com.crayonsmp.neomUtilities.utils;

import net.md_5.bungee.api.ChatColor; // WICHTIG: Der richtige Import
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {

    private static final Pattern START_WITH_COLOR_PATTERN = Pattern.compile(
            "^(?:[&§][0-9a-fk-or]|#[a-fA-F0-9]{6}).*",
            Pattern.CASE_INSENSITIVE
    );

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(format(message));
    }

    public static String format(String message) {
        if (message == null) return "";

        // Zuerst Hex umwandeln (bevor &7 davor geklatscht wird)
        message = hex(message);

        Matcher matcher = START_WITH_COLOR_PATTERN.matcher(message);
        if (!matcher.matches()) {
            message = "&7" + message;
        }

        // Nutzt die statische Methode der Bungee-API
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String hex(String message) {
        // Sucht nach <#RRGGBB>
        Pattern pattern = Pattern.compile("<(#?[a-fA-F0-9]{6})>");
        Matcher matcher = pattern.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            // Falls das # fehlt, fügen wir es für die API hinzu
            if (!hexCode.startsWith("#")) hexCode = "#" + hexCode;

            matcher.appendReplacement(result, ChatColor.of(hexCode).toString());
        }

        matcher.appendTail(result);
        return result.toString();
    }
}