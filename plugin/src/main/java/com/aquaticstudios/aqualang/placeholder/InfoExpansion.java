package com.aquaticstudios.aqualang.placeholder;

import com.aquaticstudios.aqualang.AquaLang;
import com.aquaticstudios.aqualang.cache.LanguageCache;
import com.aquaticstudios.aqualang.language.LanguageManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InfoExpansion extends PlaceholderExpansion {

    private final AquaLang plugin;
    private final LanguageManager languageManager;
    private final LanguageCache cache;

    public InfoExpansion(AquaLang plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.cache = plugin.getLanguageCache();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "aqualang";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Senkex";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null || params.isEmpty()) return "";

        String lang = cache.get(player.getUniqueId());

        switch (params.toLowerCase()) {
            case "language":
                return lang;
            case "locale": {
                String locale = languageManager.getLocaleOf(lang);
                return locale != null ? locale : "";
            }
            case "default":
                return languageManager.getDefaultLang();
            case "registered":
                return String.valueOf(languageManager.getRegisteredLanguages().size());
            default:
                return "";
        }
    }
}
