package com.aquaticstudios.aqualang.command;

import com.aquaticstudios.aqualang.AquaLang;
import com.aquaticstudios.aqualang.cache.LanguageCache;
import com.aquaticstudios.aqualang.github.GitHubConfig;
import com.aquaticstudios.aqualang.github.GitHubSyncResult;
import com.aquaticstudios.aqualang.language.LanguageHandler;
import com.aquaticstudios.aqualang.language.LanguageManager;
import com.aquaticstudios.aqualang.util.ColorUtil;
import com.aquaticstudios.aqualang.util.VersionUtil;
import org.bukkit.Bukkit;
import com.aquaticstudios.aqualang.util.Scheduler;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public final class AquaLangCommand implements CommandExecutor {

    private final AquaLang plugin;
    private final String pluginVersion;
    private final LanguageManager languageManager;
    private final LanguageHandler lang;
    private final LanguageCache cache;

    public AquaLangCommand(AquaLang plugin) {
        this.plugin = plugin;
        this.pluginVersion = plugin.getDescription().getVersion();
        this.languageManager = plugin.getLanguageManager();
        this.lang = plugin.getLanguageHandler();
        this.cache = plugin.getLanguageCache();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String userLang = resolveUserLang(sender);

        if (args.length == 0) {
            sender.sendMessage("");
            sender.sendMessage(ColorUtil.colorize("            &#35ADFF&lAquaLang &fversion &#8DFF87[" + pluginVersion + "] &7(" + VersionUtil.getVersion() + ")"));
            sender.sendMessage(ColorUtil.colorize("           &fPowered by &#8BD5FFSenkex @ Aquatic Studios"));
            sender.sendMessage("");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "help": {
                sendHelp(sender, userLang);
                return true;
            }

            case "list": {
                if (denied(sender, userLang, "list")) return true;

                sender.sendMessage(lang.get(userLang, userLang, "list.header"));

                String fmt = lang.get(userLang, userLang, "list.format");
                for (Map.Entry<String, String> entry : languageManager.getLanguageMap().entrySet()) {
                    sender.sendMessage(fmt
                            .replace("{locale}", entry.getKey())
                            .replace("{language}", entry.getValue()));
                }
                return true;
            }

            case "aliases": {
                if (denied(sender, userLang, "aliases")) return true;

                sender.sendMessage(lang.get(userLang, userLang, "aliases.header"));
                String fmt = lang.get(userLang, userLang, "aliases.format");
                for (String cmd : plugin.getConfig().getStringList("commands")) {
                    sender.sendMessage(fmt.replace("{command}", "/" + cmd));
                }
                return true;
            }

            case "set": {
                if (denied(sender, userLang, "set")) return true;

                if (args.length != 3) {
                    sender.sendMessage(lang.get(userLang, userLang, "usage.set"));
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                String targetName = target.getName() != null ? target.getName() : args[1];
                UUID uuid = target.getUniqueId();

                String resolved = languageManager.resolveLanguageStrict(args[2]);
                if (resolved == null) {
                    sender.sendMessage(lang.get(userLang, userLang, "errors.language-not-found")
                            .replace("{input}", args[2]));
                    return true;
                }

                cache.setAsync(uuid, resolved).whenComplete((v, ex) ->
                        Scheduler.global(plugin, () -> {
                            if (ex != null) {
                                sender.sendMessage(lang.get(userLang, userLang, "errors.language-not-found")
                                        .replace("{input}", args[2]));
                                return;
                            }
                            sender.sendMessage(lang.get(userLang, userLang, "success.other-language-set")
                                    .replace("{player}", targetName)
                                    .replace("{language}", resolved));
                        })
                );
                return true;
            }

            case "reset": {
                if (denied(sender, userLang, "reset")) return true;

                if (args.length != 2) {
                    sender.sendMessage(lang.get(userLang, userLang, "usage.reset"));
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                String targetName = target.getName() != null ? target.getName() : args[1];
                String defaultLang = languageManager.getDefaultLang();

                cache.setAsync(target.getUniqueId(), defaultLang).whenComplete((v, ex) ->
                        Scheduler.global(plugin, () ->
                                sender.sendMessage(lang.get(userLang, userLang, "success.reset")
                                        .replace("{player}", targetName))
                        )
                );
                return true;
            }

            case "github": {
                if (denied(sender, userLang, "github")) return true;

                if (args.length != 2) {
                    sender.sendMessage(lang.get(userLang, userLang, "github.usage"));
                    return true;
                }

                if (args[1].equalsIgnoreCase("sync")) {
                    sender.sendMessage(lang.get(userLang, userLang, "github.sync.start"));

                    plugin.getGitHubSyncManager().syncAsync().whenComplete((result, ex) ->
                            Scheduler.global(plugin, () -> {
                                if (ex != null || result == GitHubSyncResult.FAILED) {
                                    sender.sendMessage(lang.get(userLang, userLang, "github.sync.failed"));
                                    return;
                                }
                                switch (result) {
                                    case SUCCESS:
                                        sender.sendMessage(lang.get(userLang, userLang, "github.sync.success"));
                                        break;
                                    case NO_CHANGES:
                                        sender.sendMessage(lang.get(userLang, userLang, "github.sync.no-changes"));
                                        break;
                                    case FAILED:
                                        sender.sendMessage(lang.get(userLang, userLang, "github.sync.failed"));
                                        break;
                                }
                            })
                    );
                    return true;
                }

                if (args[1].equalsIgnoreCase("status")) {
                    GitHubConfig cfg;
                    try {
                        cfg = GitHubConfig.load(plugin.getConfig());
                    } catch (Exception e) {
                        sender.sendMessage(ColorUtil.colorize(
                                "&#FF3535GitHub config missing or invalid: &#FFD935" + e.getMessage()));
                        return true;
                    }

                    sender.sendMessage(lang.get(userLang, userLang, "github.status.header"));
                    sender.sendMessage(lang.get(userLang, userLang, "github.status.repository")
                            .replace("{repo}", cfg.repository));
                    sender.sendMessage(lang.get(userLang, userLang, "github.status.branch")
                            .replace("{branch}", cfg.branch));
                    sender.sendMessage(lang.get(userLang, userLang, "github.status.auth")
                            .replace("{auth}", cfg.authType.name().toLowerCase()));
                    sender.sendMessage(lang.get(userLang, userLang, "github.status.remote-root")
                            .replace("{path}", cfg.remoteRoot));
                    sender.sendMessage(lang.get(userLang, userLang, "github.status.local-root")
                            .replace("{path}", cfg.localRoot));
                    sender.sendMessage(lang.get(userLang, userLang, "github.status.create-missing")
                            .replace("{value}", String.valueOf(cfg.createMissing)));
                    sender.sendMessage(lang.get(userLang, userLang, "github.status.overwrite-existing")
                            .replace("{value}", String.valueOf(cfg.overwriteExisting)));
                    sender.sendMessage(lang.get(userLang, userLang, "github.status.delete-missing")
                            .replace("{value}", String.valueOf(cfg.deleteMissing)));
                    sender.sendMessage(lang.get(userLang, userLang, "github.status.reload-after-sync")
                            .replace("{value}", String.valueOf(cfg.reloadAfterSync)));
                    return true;
                }

                sender.sendMessage(lang.get(userLang, userLang, "github.usage"));
                return true;
            }

            case "info": {
                if (denied(sender, userLang, "info")) return true;

                if (args.length != 2) {
                    sender.sendMessage(lang.get(userLang, userLang, "usage.info"));
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                String targetName = target.getName() != null ? target.getName() : args[1];

                Scheduler.async(plugin, () -> {
                    String targetLang = cache.getOrFetch(target.getUniqueId());
                    if (!languageManager.isRegisteredLanguage(targetLang)) {
                        targetLang = languageManager.getDefaultLang();
                    }
                    String finalLang = targetLang;
                    Scheduler.global(plugin, () ->
                            sender.sendMessage(lang.get(userLang, userLang, "info.format")
                                    .replace("{player}", targetName)
                                    .replace("{language}", finalLang))
                    );
                });
                return true;
            }

            case "reload": {
                if (denied(sender, userLang, "reload")) return true;

                plugin.reloadSettings();
                languageManager.reloadLanguages(plugin.getConfig());

                sender.sendMessage(lang.get(userLang, userLang, "success.reload"));
                return true;
            }

            default: {
                sendHelp(sender, userLang);
                return true;
            }
        }
    }

    private boolean denied(CommandSender sender, String userLang, String sub) {
        if (sender.hasPermission("aqualang." + sub) || sender.hasPermission("aqualang.admin")) {
            return false;
        }
        sender.sendMessage(lang.get(userLang, userLang, "errors.no-permission"));
        return true;
    }

    private String resolveUserLang(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String langCode = cache.get(player.getUniqueId());
            if (languageManager.isRegisteredLanguage(langCode)) {
                return langCode;
            }
        }
        return languageManager.getDefaultLang();
    }

    private void sendHelp(CommandSender sender, String userLang) {
        String helpText = lang.get(userLang, userLang, "help");

        for (String line : helpText.split("\n")) {
            if (line.trim().equalsIgnoreCase("<empty>")) {
                sender.sendMessage("");
                continue;
            }
            sender.sendMessage(line.replace("{version}", plugin.getDescription().getVersion()));
        }
    }
}
