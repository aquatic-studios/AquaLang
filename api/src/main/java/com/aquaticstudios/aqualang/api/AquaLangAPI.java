package com.aquaticstudios.aqualang.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API for AquaLang: read translations, change a player's language and
 * query the registered languages.
 *
 * <p>Grab the instance with {@link #api()} once AquaLang is enabled, or use the
 * static shortcuts such as {@link #translate(Player, String)}. Declare AquaLang
 * in your plugin.yml {@code depend} or {@code softdepend}.</p>
 *
 * Developed by @Senkex
 */
public interface AquaLangAPI {

    /*
     * Current API version. Increased whenever methods are added.
     */
    int API_VERSION = 3;

    /** Namespace (language file name, without {@code .yml}) used when none is given. */
    String DEFAULT_NAMESPACE = "aquaaddon";

    /**
     * Translates {@code path} for a player using the default namespace.
     *
     * @param player the player whose language is used
     * @param path the dotted key inside the language file
     * @return the translated, color-formatted message
     */
    @NotNull String message(@NotNull Player player, @NotNull String path);

    /**
     * Translates {@code path} for a UUID using the default namespace.
     *
     * @param uuid the player UUID
     * @param path the dotted key inside the language file
     * @return the translated, color-formatted message
     */
    @NotNull String message(@NotNull UUID uuid, @NotNull String path);

    /**
     * Translates {@code path} in a specific language (default namespace).
     *
     * @param langInput a language name or locale (e.g. "english" or "en_US")
     * @param path the dotted key inside the language file
     * @return the translated, color-formatted message
     */
    @NotNull String messageInLang(@NotNull String langInput, @NotNull String path);

    /**
     * Translates {@code path} for a player inside a custom namespace.
     *
     * @param player the player whose language is used
     * @param namespace the addon namespace (language file name without {@code .yml})
     * @param path the dotted key inside the language file
     * @return the translated, color-formatted message
     */
    @NotNull String message(@NotNull Player player, @NotNull String namespace, @NotNull String path);

    /**
     * Translates {@code path} for a UUID inside a custom namespace.
     *
     * @param uuid the player UUID
     * @param namespace the addon namespace
     * @param path the dotted key inside the language file
     * @return the translated, color-formatted message
     */
    @NotNull String message(@NotNull UUID uuid, @NotNull String namespace, @NotNull String path);

    /**
     * Translates {@code path} in a specific language and namespace.
     *
     * @param langInput a language name or locale
     * @param namespace the addon namespace
     * @param path the dotted key inside the language file
     * @return the translated, color-formatted message
     */
    @NotNull String messageInLang(@NotNull String langInput, @NotNull String namespace, @NotNull String path);

    /**
     * Like {@link #message(UUID, String, String)} but returns {@code fallback} when the key is missing.
     *
     * @param uuid the player UUID
     * @param namespace the addon namespace
     * @param path the dotted key inside the language file
     * @param fallback value returned when the key does not exist
     * @return the translated message, or {@code fallback}
     */
    @NotNull String messageOrDefault(@NotNull UUID uuid, @NotNull String namespace, @NotNull String path, @NotNull String fallback);

    /**
     * Translates, applies placeholders, colorizes and sends a message to the player.
     *
     * @param player the recipient (its language is used)
     * @param path the dotted key inside the language file
     * @param placeholders alternating search/replace pairs, e.g. {@code "<lang>", "english"}
     */
    void send(@NotNull Player player, @NotNull String path, @NotNull String... placeholders);

    /**
     * Applies color codes ({@code &#RRGGBB} hex, MiniMessage and legacy {@code &}) to a string.
     *
     * @param message the raw message
     * @return the colorized message
     */
    @NotNull String colorize(@NotNull String message);

    /**
     * Checks whether a key exists in a language (default namespace).
     *
     * @param langInput a language name or locale
     * @param path the dotted key inside the language file
     * @return {@code true} if the key exists
     */
    boolean has(@NotNull String langInput, @NotNull String path);

    /**
     * Checks whether a key exists in a language and namespace.
     *
     * @param langInput a language name or locale
     * @param namespace the addon namespace
     * @param path the dotted key inside the language file
     * @return {@code true} if the key exists
     */
    boolean has(@NotNull String langInput, @NotNull String namespace, @NotNull String path);

    /**
     * Sets a player's language, firing a {@link com.aquaticstudios.aqualang.api.event.PlayerLanguageChangeEvent}.
     *
     * @param player the player
     * @param langInput a language name or locale
     * @return {@code true} if the language is registered and was applied
     */
    boolean setPlayerLanguage(@NotNull Player player, @NotNull String langInput);

    /**
     * Sets a player's language by UUID, firing a {@link com.aquaticstudios.aqualang.api.event.PlayerLanguageChangeEvent}.
     *
     * @param uuid the player UUID
     * @param langInput a language name or locale
     * @return {@code true} if the language is registered and was applied
     */
    boolean setPlayerLanguage(@NotNull UUID uuid, @NotNull String langInput);

    /**
     * Sets a player's language and persists it off the main thread.
     *
     * @param uuid the player UUID
     * @param langInput a language name or locale
     * @return a future completing with {@code true} on success
     */
    @NotNull CompletableFuture<Boolean> setPlayerLanguageAsync(@NotNull UUID uuid, @NotNull String langInput);

    /**
     * Returns a player's current language, or the default if none is set.
     *
     * @param player the player
     * @return the language name
     */
    @NotNull String getPlayerLanguage(@NotNull Player player);

    /**
     * Returns a player's current language by UUID, or the default if none is set.
     *
     * @param uuid the player UUID
     * @return the language name
     */
    @NotNull String getPlayerLanguage(@NotNull UUID uuid);

    /**
     * Checks whether a language name or locale is registered.
     *
     * @param langInput a language name or locale
     * @return {@code true} if registered
     */
    boolean isLanguageRegistered(@NotNull String langInput);

    /**
     * Alias of {@link #isLanguageRegistered(String)}.
     *
     * @param langInput a language name or locale
     * @return {@code true} if registered
     */
    boolean supports(@NotNull String langInput);

    /**
     * @return the registered language names (e.g. "english", "spanish")
     */
    @NotNull Set<String> getRegisteredLanguages();

    /**
     * @return the registered locales (e.g. "en_US", "es_ES")
     */
    @NotNull Set<String> getRegisteredLocales();

    /**
     * @return the default language name from the config
     */
    @NotNull String getDefaultLanguage();

    /**
     * Returns the locale mapped to a language name.
     *
     * @param folder the language name (its folder)
     * @return the locale, or {@code null} if unknown
     */
    @Nullable String getLocaleOf(@NotNull String folder);

    /**
     * Filters the given candidates to those registered in AquaLang.
     *
     * @param candidates language names or locales to test
     * @return the subset that AquaLang recognises
     */
    @NotNull Set<String> compatibleLanguages(@NotNull Collection<String> candidates);

    /**
     * @return the running API version (see {@link #API_VERSION})
     */
    int getApiVersion();

    /**
     * Returns the active API instance.
     *
     * @return the registered {@link AquaLangAPI}
     * @throws IllegalStateException if AquaLang has not enabled yet
     */
    @NotNull
    static AquaLangAPI api() {
        return AquaLangProvider.get();
    }

    /**
     * @return {@code true} once AquaLang has registered the API
     */
    static boolean isAvailable() {
        return AquaLangProvider.isRegistered();
    }

    /**
     * Ensures the running API is at least {@code minVersion}.
     *
     * @param minVersion the minimum required version
     * @throws IllegalStateException if the running version is lower
     */
    static void requireVersion(int minVersion) {
        if (API_VERSION < minVersion) {
            throw new IllegalStateException(
                    "AquaAPI version " + API_VERSION + " is lower than required " + minVersion
            );
        }
    }

    /**
     * Static shortcut for {@link #message(Player, String)}.
     *
     * @param player the player whose language is used
     * @param path the dotted key
     * @return the translated message
     */
    @NotNull
    static String translate(@NotNull Player player, @NotNull String path) {
        return api().message(player, path);
    }

    /**
     * Static shortcut for {@link #message(UUID, String)}.
     *
     * @param uuid the player UUID
     * @param path the dotted key
     * @return the translated message
     */
    @NotNull
    static String translate(@NotNull UUID uuid, @NotNull String path) {
        return api().message(uuid, path);
    }

    /**
     * Static shortcut for {@link #message(Player, String, String)}.
     *
     * @param player the player whose language is used
     * @param namespace the addon namespace
     * @param path the dotted key
     * @return the translated message
     */
    @NotNull
    static String translate(@NotNull Player player, @NotNull String namespace, @NotNull String path) {
        return api().message(player, namespace, path);
    }

    /**
     * Static shortcut for {@link #message(UUID, String, String)}.
     *
     * @param uuid the player UUID
     * @param namespace the addon namespace
     * @param path the dotted key
     * @return the translated message
     */
    @NotNull
    static String translate(@NotNull UUID uuid, @NotNull String namespace, @NotNull String path) {
        return api().message(uuid, namespace, path);
    }

    /**
     * Static shortcut for {@link #colorize(String)}.
     *
     * @param message the raw message
     * @return the colorized message
     */
    @NotNull
    static String color(@NotNull String message) {
        return api().colorize(message);
    }

    /**
     * Static shortcut for {@link #messageInLang(String, String)}.
     *
     * @param langInput a language name or locale
     * @param path the dotted key
     * @return the translated message
     */
    @NotNull
    static String translateInLang(@NotNull String langInput, @NotNull String path) {
        return api().messageInLang(langInput, path);
    }

    /**
     * Static shortcut for {@link #messageInLang(String, String, String)}.
     *
     * @param langInput a language name or locale
     * @param namespace the addon namespace
     * @param path the dotted key
     * @return the translated message
     */
    @NotNull
    static String translateInLang(@NotNull String langInput, @NotNull String namespace, @NotNull String path) {
        return api().messageInLang(langInput, namespace, path);
    }

    /**
     * Static shortcut for {@link #messageOrDefault(UUID, String, String, String)}.
     *
     * @param uuid the player UUID
     * @param namespace the addon namespace
     * @param path the dotted key
     * @param fallback value returned when the key does not exist
     * @return the translated message, or {@code fallback}
     */
    @NotNull
    static String translateOrDefault(@NotNull UUID uuid, @NotNull String namespace, @NotNull String path, @NotNull String fallback) {
        return api().messageOrDefault(uuid, namespace, path, fallback);
    }

    /**
     * Static shortcut for {@link #has(String, String)}.
     *
     * @param langInput a language name or locale
     * @param path the dotted key
     * @return {@code true} if the key exists
     */
    static boolean exists(@NotNull String langInput, @NotNull String path) {
        return api().has(langInput, path);
    }

    /**
     * Static shortcut for {@link #has(String, String, String)}.
     *
     * @param langInput a language name or locale
     * @param namespace the addon namespace
     * @param path the dotted key
     * @return {@code true} if the key exists
     */
    static boolean exists(@NotNull String langInput, @NotNull String namespace, @NotNull String path) {
        return api().has(langInput, namespace, path);
    }

    /**
     * Static shortcut for {@link #setPlayerLanguage(Player, String)}.
     *
     * @param player the player
     * @param langInput a language name or locale
     * @return {@code true} if applied
     */
    static boolean setLanguage(@NotNull Player player, @NotNull String langInput) {
        return api().setPlayerLanguage(player, langInput);
    }

    /**
     * Static shortcut for {@link #setPlayerLanguage(UUID, String)}.
     *
     * @param uuid the player UUID
     * @param langInput a language name or locale
     * @return {@code true} if applied
     */
    static boolean setLanguage(@NotNull UUID uuid, @NotNull String langInput) {
        return api().setPlayerLanguage(uuid, langInput);
    }

    /**
     * Static shortcut for {@link #setPlayerLanguageAsync(UUID, String)}.
     *
     * @param uuid the player UUID
     * @param langInput a language name or locale
     * @return a future completing with {@code true} on success
     */
    @NotNull
    static CompletableFuture<Boolean> setLanguageAsync(@NotNull UUID uuid, @NotNull String langInput) {
        return api().setPlayerLanguageAsync(uuid, langInput);
    }

    /**
     * Static shortcut for {@link #getPlayerLanguage(Player)}.
     *
     * @param player the player
     * @return the language name
     */
    @NotNull
    static String getLanguage(@NotNull Player player) {
        return api().getPlayerLanguage(player);
    }

    /**
     * Static shortcut for {@link #getPlayerLanguage(UUID)}.
     *
     * @param uuid the player UUID
     * @return the language name
     */
    @NotNull
    static String getLanguage(@NotNull UUID uuid) {
        return api().getPlayerLanguage(uuid);
    }

    /**
     * Static shortcut for {@link #getRegisteredLanguages()}.
     *
     * @return the registered language names
     */
    @NotNull
    static Set<String> getRegistered() {
        return api().getRegisteredLanguages();
    }

    /**
     * Static shortcut for {@link #getRegisteredLocales()}.
     *
     * @return the registered locales
     */
    @NotNull
    static Set<String> getLocales() {
        return api().getRegisteredLocales();
    }

    /**
     * Static shortcut for {@link #getDefaultLanguage()}.
     *
     * @return the default language name
     */
    @NotNull
    static String getDefault() {
        return api().getDefaultLanguage();
    }
}
