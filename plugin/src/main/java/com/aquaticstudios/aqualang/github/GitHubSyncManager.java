package com.aquaticstudios.aqualang.github;

import com.aquaticstudios.aqualang.AquaLang;
import com.aquaticstudios.aqualang.language.LanguageManager;
import org.bukkit.Bukkit;
import com.aquaticstudios.aqualang.util.Scheduler;

import java.util.concurrent.CompletableFuture;

public final class GitHubSyncManager {

    private final AquaLang plugin;
    private final LanguageManager languageManager;

    public GitHubSyncManager(AquaLang plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
    }

    public GitHubSyncResult sync() {
        try {
            GitHubConfig cfg = GitHubConfig.load(plugin.getConfig());
            GitHubSynchronizer synchronizer =
                    new GitHubSynchronizer(cfg, plugin.getDataFolder());

            GitHubSyncResult result = synchronizer.execute();

            if (result == GitHubSyncResult.SUCCESS && cfg.reloadAfterSync) {
                Scheduler.global(plugin, () ->
                        languageManager.reloadLanguages(plugin.getConfig())
                );
            }

            return result;

        } catch (Exception e) {
            plugin.getLogger().warning("GitHub sync failed: " + e.getMessage());
            return GitHubSyncResult.FAILED;
        }
    }

    public CompletableFuture<GitHubSyncResult> syncAsync() {
        CompletableFuture<GitHubSyncResult> future = new CompletableFuture<>();
        Scheduler.async(plugin, () ->
                future.complete(sync())
        );
        return future;
    }
}
