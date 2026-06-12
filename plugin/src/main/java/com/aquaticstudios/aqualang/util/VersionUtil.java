package com.aquaticstudios.aqualang.util;

import org.bukkit.Bukkit;

public final class VersionUtil {

    private static final String VERSION =
            Bukkit.getBukkitVersion().split("-")[0];

    private VersionUtil() {
    }

    public static String getVersion() {
        return VERSION;
    }

}