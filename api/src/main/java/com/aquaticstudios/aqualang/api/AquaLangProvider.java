package com.aquaticstudios.aqualang.api;

import org.jetbrains.annotations.NotNull;

public final class AquaLangProvider {

    private static volatile AquaLangAPI instance;

    private AquaLangProvider() {}

    @NotNull
    public static AquaLangAPI get() {
        AquaLangAPI api = instance;
        if (api == null) {
            throw new IllegalStateException("AquaLangAPI is not registered. Make sure AquaLang is enabled and your plugin declares it in 'depend' or 'softdepend'."
            );
        }
        return api;
    }

    public static boolean isRegistered() {
        return instance != null;
    }

    public static void register(@NotNull AquaLangAPI api) {
        instance = api;
    }

    public static void unregister() {
        instance = null;
    }
}
