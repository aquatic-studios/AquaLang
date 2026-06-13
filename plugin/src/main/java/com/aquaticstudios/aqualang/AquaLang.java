package com.aquaticstudios.aqualang;

import com.aquaticstudios.aqualang.api.AquaLangAPIImpl;
import com.aquaticstudios.aqualang.api.AquaLangProvider;
import com.aquaticstudios.aqualang.cache.LanguageCache;
import com.aquaticstudios.aqualang.command.AquaLangCommand;
import com.aquaticstudios.aqualang.command.AquaLangTabCompleter;
import com.aquaticstudios.aqualang.command.alias.AliasRegister;
import com.aquaticstudios.aqualang.config.PluginSettings;
import com.aquaticstudios.aqualang.database.DatabaseManager;
import com.aquaticstudios.aqualang.database.DatabaseType;
import com.aquaticstudios.aqualang.placeholder.InfoExpansion;
import com.aquaticstudios.aqualang.placeholder.MessageExpansion;
import com.aquaticstudios.aqualang.github.GitHubSyncManager;
import com.aquaticstudios.aqualang.language.LanguageFileLoader;
import com.aquaticstudios.aqualang.language.LanguageFileWatcher;
import com.aquaticstudios.aqualang.language.LanguageHandler;
import com.aquaticstudios.aqualang.language.LanguageManager;
import com.aquaticstudios.aqualang.library.Libraries;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.zapper.DependencyManager;
import revxrsal.zapper.classloader.URLClassLoaderWrapper;
import revxrsal.zapper.relocation.Relocation;
import revxrsal.zapper.repository.Repository;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Map;

public final class AquaLang extends JavaPlugin {

    private static AquaLang instance;

    private PluginSettings settings;
    private LanguageFileLoader languageFileLoader;
    private LanguageManager languageManager;
    private LanguageHandler languageHandler;
    private LanguageFileWatcher fileWatcher;
    private LanguageCache languageCache;
    private GitHubSyncManager gitHubSyncManager;

    @Override
    public void onLoad() {
        File libs = new File(getDataFolder(), "libraries");
        if (!libs.exists() && !libs.mkdirs()) {
            getLogger().warning("Could not create libraries folder");
        }

        DatabaseType dbType = resolveDatabaseType();
        getLogger().info("Installing runtime dependencies for database: " + dbType.name().toLowerCase() + "...");

        DependencyManager manager = new DependencyManager(
                libs,
                URLClassLoaderWrapper.wrap((URLClassLoader) getClassLoader())
        );

        manager.repository(Repository.mavenCentral());
        manager.repository(Repository.maven("https://repo.codemc.org/repository/maven-public/"));

        for (Libraries lib : Libraries.values()) {
            if (!lib.isNeededFor(dbType)) continue;
            manager.dependency(lib.dependency());
            for (Relocation relocation : lib.relocations()) {
                manager.relocate(relocation);
            }
        }

        manager.load();
    }

    private DatabaseType resolveDatabaseType() {
        String typeName = getConfig().getString("database.type", "SQLITE");
        try {
            return DatabaseType.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DatabaseType.SQLITE;
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        new Metrics(this, 31941);

        saveDefaultConfig();
        settings = new PluginSettings(getConfig());

        File baseLangFolder = new File(getDataFolder(), "languages");
        if (!baseLangFolder.exists() && !baseLangFolder.mkdirs()) {
            getLogger().warning("Could not create languages folder");
        }

        languageFileLoader = new LanguageFileLoader(baseLangFolder);
        languageManager = new LanguageManager(languageFileLoader);
        languageHandler = new LanguageHandler(languageFileLoader, languageManager);

        languageManager.loadLanguagesFromConfig(getConfig());
        logRegisteredLanguages();

        if (settings.isAutoReloadLanguages()) {
            fileWatcher = new LanguageFileWatcher(this, languageFileLoader, baseLangFolder);
            fileWatcher.start();
        } else {
            getLogger().info("Auto-reload disabled in config. Use /aqualang reload to apply YAML changes.");
        }

        DatabaseManager.loadDatabase();

        languageCache = new LanguageCache(this, languageManager);

        AquaLangProvider.register(new AquaLangAPIImpl(this));
        AliasRegister.registerLanguageCommands(this);

        gitHubSyncManager = new GitHubSyncManager(this);

        getCommand("aqualang").setExecutor(new AquaLangCommand(this));
        getCommand("aqualang").setTabCompleter(new AquaLangTabCompleter(this));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MessageExpansion(this).register();
            new InfoExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        } else {
            getLogger().warning("PlaceholderAPI not found.");
        }

        getLogger().info("AquaLang " + getDescription().getVersion() + " enabled successfully.");
    }

    @Override
    public void onDisable() {
        AquaLangProvider.unregister();
        if (fileWatcher != null) fileWatcher.stop();
        if (languageCache != null) languageCache.clear();
        DatabaseManager.close();
    }

    public void reloadSettings() {
        reloadConfig();
        settings = new PluginSettings(getConfig());
    }

    public static AquaLang getInstance() {
        return instance;
    }

    public PluginSettings getSettings() {
        return settings;
    }

    public LanguageFileLoader getLanguageFileLoader() {
        return languageFileLoader;
    }

    public FileConfiguration getMainConfig() {
        return getConfig();
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public LanguageHandler getLanguageHandler() {
        return languageHandler;
    }

    public LanguageCache getLanguageCache() {
        return languageCache;
    }

    public GitHubSyncManager getGitHubSyncManager() {
        return gitHubSyncManager;
    }

    private void logRegisteredLanguages() {
        Map<String, String> map = languageManager.getLanguageMap();

        getLogger().info("Languages available (" + map.size() + "):");

        for (Map.Entry<String, String> entry : map.entrySet()) {
            getLogger().info("  - " + entry.getKey().toUpperCase()
                    + " (" + entry.getValue() + ")");
        }
    }
}
