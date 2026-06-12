package com.aquaticstudios.aqualang.addon;

import com.aquaticstudios.aqualang.addon.command.AquaAddonCommand;
import com.aquaticstudios.aqualang.addon.config.MenuConfigGenerator;
import com.aquaticstudios.aqualang.addon.listener.FirstJoinListener;
import com.aquaticstudios.aqualang.addon.listener.MenuListener;
import com.aquaticstudios.aqualang.api.AquaLangAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public final class AquaLangAddon extends JavaPlugin {

    private static final int REQUIRED_API_VERSION = 3;

    private static AquaLangAddon instance;

    public static AquaLangAddon get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (!ensureAquaLang()) return;

        int generated = MenuConfigGenerator.generateForAllLanguages();
        getLogger().info("Menu files ready (" + generated + " generated, "
                + AquaLangAPI.getRegistered().size() + " languages detected).");

        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new FirstJoinListener(), this);

        registerCommand();
    }

    @Override
    public void onDisable() {
        getLogger().info("AquaLangAddon disabled.");
    }

    private boolean ensureAquaLang() {
        Plugin aquaLang = Bukkit.getPluginManager().getPlugin("AquaLang");
        if (aquaLang == null || !aquaLang.isEnabled()) {
            severe("AquaLang is NOT installed or enabled.",
                    "This plugin requires AquaLang to work.",
                    "Please download AquaLang and restart.");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }

        if (!AquaLangAPI.isAvailable()) {
            severe("AquaLang is loaded but the API is not registered.",
                    "Make sure you are running AquaLang v1.0.0 or newer.");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }

        try {
            AquaLangAPI.requireVersion(REQUIRED_API_VERSION);
        } catch (IllegalStateException e) {
            severe("AquaLangAddon requires AquaLang API v" + REQUIRED_API_VERSION + "+.",
                    e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }

        return true;
    }

    private void severe(String... lines) {
        for (String line : lines) getLogger().severe(" " + line);
    }

    private void registerCommand() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            AquaAddonCommand executor = new AquaAddonCommand();

            Command cmd = new Command("aquaaddon") {
                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    return executor.onCommand(sender, this, label, args);
                }
            };

            cmd.setPermission("aquaaddon.menu");
            cmd.setDescription("Open language menu.");

            commandMap.register("aquaaddon", cmd);

        } catch (Exception e) {
            getLogger().warning("Failed to register /aquaaddon command: " + e.getMessage());
        }
    }
}
