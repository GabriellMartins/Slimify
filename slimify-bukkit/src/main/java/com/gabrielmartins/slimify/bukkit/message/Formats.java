package com.gabrielmartins.slimify.bukkit.message;

import java.util.Locale;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Formats {

    private final String[] UNITS = {"B", "KB", "MB", "GB"};

    public String bytes(long bytes) {
        double value = bytes;
        int unit = 0;
        while (value >= 1024 && unit < UNITS.length - 1) {
            value /= 1024;
            unit++;
        }
        return unit == 0 ? bytes + " B" : String.format(Locale.ROOT, "%.1f %s", value, UNITS[unit]);
    }
}
