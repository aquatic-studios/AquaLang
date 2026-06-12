package com.aquaticstudios.aqualang.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AquaLangAPI {

    int API_VERSION = 3;

    String DEFAULT_NAMESPACE = "aquaaddon";

    @NotNull String message(@NotNull Player player, @NotNull String path);

    @NotNull String message(@NotNull UUID uuid, @NotNull String path);

    @NotNull String messageInLang(@NotNull String langInput, @NotNull String path);

    @NotNull String message(@NotNull Player player, @NotNull String namespace, @NotNull String path);

    @NotNull String message(@NotNull UUID uuid, @NotNull String namespace, @NotNull String path);

    @NotNull String messageInLang(@NotNull String langInput, @NotNull String namespace, @NotNull String path);

    @NotNull String messageOrDefault(@NotNull UUID uuid, @NotNull String namespace, @NotNull String path, @NotNull String fallback);

    void send(@NotNull Player player, @NotNull String path, @NotNull String... placeholders);

    @NotNull String colorize(@NotNull String message);

    boolean has(@NotNull String langInput, @NotNull String path);

    boolean has(@NotNull String langInput, @NotNull String namespace, @NotNull String path);

    boolean setPlayerLanguage(@NotNull Player player, @NotNull String langInput);

    boolean setPlayerLanguage(@NotNull UUID uuid, @NotNull String langInput);

    @NotNull CompletableFuture<Boolean> setPlayerLanguageAsync(@NotNull UUID uuid, @NotNull String langInput);

    @NotNull String getPlayerLanguage(@NotNull Player player);

    @NotNull String getPlayerLanguage(@NotNull UUID uuid);

    boolean isLanguageRegistered(@NotNull String langInput);

    boolean supports(@NotNull String langInput);

    @NotNull Set<String> getRegisteredLanguages();

    @NotNull Set<String> getRegisteredLocales();

    @NotNull String getDefaultLanguage();

    @Nullable String getLocaleOf(@NotNull String folder);

    @NotNull Set<String> compatibleLanguages(@NotNull Collection<String> candidates);

    int getApiVersion();

    @NotNull
    static AquaLangAPI api() {
        return AquaLangProvider.get();
    }

    static boolean isAvailable() {
        return AquaLangProvider.isRegistered();
    }

    static void requireVersion(int minVersion) {
        if (API_VERSION < minVersion) {
            throw new IllegalStateException(
                    "AquaAPI version " + API_VERSION + " is lower than required " + minVersion
            );
        }
    }

    @NotNull
    static String translate(@NotNull Player player, @NotNull String path) {
        return api().message(player, path);
    }

    @NotNull
    static String translate(@NotNull UUID uuid, @NotNull String path) {
        return api().message(uuid, path);
    }

    @NotNull
    static String translate(@NotNull Player player, @NotNull String namespace, @NotNull String path) {
        return api().message(player, namespace, path);
    }

    @NotNull
    static String translate(@NotNull UUID uuid, @NotNull String namespace, @NotNull String path) {
        return api().message(uuid, namespace, path);
    }

    @NotNull
    static String color(@NotNull String message) {
        return api().colorize(message);
    }

    @NotNull
    static String translateInLang(@NotNull String langInput, @NotNull String path) {
        return api().messageInLang(langInput, path);
    }

    @NotNull
    static String translateInLang(@NotNull String langInput, @NotNull String namespace, @NotNull String path) {
        return api().messageInLang(langInput, namespace, path);
    }

    @NotNull
    static String translateOrDefault(@NotNull UUID uuid, @NotNull String namespace, @NotNull String path, @NotNull String fallback) {
        return api().messageOrDefault(uuid, namespace, path, fallback);
    }

    static boolean exists(@NotNull String langInput, @NotNull String path) {
        return api().has(langInput, path);
    }

    static boolean exists(@NotNull String langInput, @NotNull String namespace, @NotNull String path) {
        return api().has(langInput, namespace, path);
    }

    static boolean setLanguage(@NotNull Player player, @NotNull String langInput) {
        return api().setPlayerLanguage(player, langInput);
    }

    static boolean setLanguage(@NotNull UUID uuid, @NotNull String langInput) {
        return api().setPlayerLanguage(uuid, langInput);
    }

    @NotNull
    static CompletableFuture<Boolean> setLanguageAsync(@NotNull UUID uuid, @NotNull String langInput) {
        return api().setPlayerLanguageAsync(uuid, langInput);
    }

    @NotNull
    static String getLanguage(@NotNull Player player) {
        return api().getPlayerLanguage(player);
    }

    @NotNull
    static String getLanguage(@NotNull UUID uuid) {
        return api().getPlayerLanguage(uuid);
    }

    @NotNull
    static Set<String> getRegistered() {
        return api().getRegisteredLanguages();
    }

    @NotNull
    static Set<String> getLocales() {
        return api().getRegisteredLocales();
    }

    @NotNull
    static String getDefault() {
        return api().getDefaultLanguage();
    }
}
