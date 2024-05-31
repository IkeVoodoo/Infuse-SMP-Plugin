package me.ikevoodoo.infusesmp.utils;

import org.bukkit.ChatColor;

public final class StringUtils {

    private StringUtils() {

    }

    public static String color(String toColor) {
        return ChatColor.translateAlternateColorCodes('&', toColor);
    }

}
