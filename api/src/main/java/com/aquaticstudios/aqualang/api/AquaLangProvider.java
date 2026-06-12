package com.aquaticstudios.aqualang.api;

import org.jetbrains.annotations.NotNull;

/**
 * Holds the {@link AquaLangAPI} singleton. Registered by AquaLang on enable and
 * unregistered on disable. Consumers should prefer {@link AquaLangAPI#api()}.
 *
 * Developed by @Senkex
 */
public final class AquaLangProvider {

    private static volatile AquaLangAPI instance;

    private AquaLangProvider() {}

    /**
     * Returns the registered API instance.
     *
     * @return the active {@link AquaLangAPI}
     * @throws IllegalStateException if AquaLang is not enabled yet
     */
    @NotNull
    public static AquaLangAPI get() {
        AquaLangAPI api = instance;
        if (api == null) {
            throw new IllegalStateException("AquaLangAPI is not registered. Make sure AquaLang is enabled and your plugin declares it in 'depend' or 'softdepend'."
            );
        }
        return api;
    }

    /**
     * @return {@code true} if the API has been registered
     */
    public static boolean isRegistered() {
        return instance != null;
    }

    /**
     * Registers the API instance. Called by AquaLang only.
     *
     * @param api the implementation to expose
     */
    public static void register(@NotNull AquaLangAPI api) {
        instance = api;
    }

    /** Clears the API instance. Called by AquaLang only. */
    public static void unregister() {
        instance = null;
    }
}
