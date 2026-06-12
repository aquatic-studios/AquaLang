package com.aquaticstudios.aqualang.language;

import com.aquaticstudios.aqualang.AquaLang;
import org.bukkit.Bukkit;
import com.aquaticstudios.aqualang.util.Scheduler;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class LanguageFileWatcher {

    private final AquaLang plugin;
    private final LanguageFileLoader loader;
    private final Path baseDir;

    private final Map<WatchKey, Path> watchedDirs = new HashMap<>();
    private final ConcurrentHashMap<String, Long> lastReload = new ConcurrentHashMap<>();

    private WatchService watchService;
    private Thread thread;
    private volatile boolean running;

    public LanguageFileWatcher(AquaLang plugin, LanguageFileLoader loader, File baseDir) {
        this.plugin = plugin;
        this.loader = loader;
        this.baseDir = baseDir.toPath();
    }

    public void start() {
        if (running) return;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            registerRecursively(baseDir);
            running = true;
            thread = new Thread(this::watchLoop, "AquaLang-FileWatcher");
            thread.setDaemon(true);
            thread.start();
            plugin.getLogger().info("Auto-reload enabled (debounce "
                    + plugin.getSettings().getReloadDebounceMs() + "ms): " + baseDir);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not start file watcher: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        if (watchService != null) {
            try { watchService.close(); } catch (IOException ignored) {}
        }
        if (thread != null) thread.interrupt();
    }

    private void registerRecursively(Path dir) throws IOException {
        try (Stream<Path> stream = Files.walk(dir)) {
            stream.filter(Files::isDirectory).forEach(this::register);
        }
    }

    private void register(Path dir) {
        try {
            WatchKey key = dir.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
            watchedDirs.put(key, dir);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not watch " + dir + ": " + e.getMessage());
        }
    }

    private void watchLoop() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                return;
            }

            Path dir = watchedDirs.get(key);
            if (dir == null) {
                key.reset();
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                Object ctx = event.context();
                if (!(ctx instanceof Path)) continue;
                Path relName = (Path) ctx;

                Path child = dir.resolve(relName);

                if (kind == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(child)) {
                    register(child);
                    continue;
                }

                String name = child.getFileName().toString().toLowerCase(Locale.ROOT);
                if (!name.endsWith(".yml")) continue;

                handle(child, kind);
            }

            if (!key.reset()) {
                watchedDirs.remove(key);
                if (watchedDirs.isEmpty()) break;
            }
        }
    }

    private void handle(Path file, WatchEvent.Kind<?> kind) {
        Path relative;
        try {
            relative = baseDir.relativize(file);
        } catch (IllegalArgumentException e) {
            return;
        }

        String relStr = relative.toString().replace("\\", "/").toLowerCase(Locale.ROOT);
        int slash = relStr.indexOf('/');
        if (slash < 0) return;

        String lang = relStr.substring(0, slash);
        String rest = relStr.substring(slash + 1);
        if (!rest.endsWith(".yml")) return;
        rest = rest.substring(0, rest.length() - 4);

        String id = lang + ":" + rest;

        long now = System.currentTimeMillis();
        long debounce = plugin.getSettings().getReloadDebounceMs();
        Long last = lastReload.get(id);
        if (last != null && now - last < debounce) return;
        lastReload.put(id, now);

        Scheduler.global(plugin, () -> {
            if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                if (plugin.getSettings().isDebug()) {
                    plugin.getLogger().info("[debug] Language file deleted: " + relStr);
                }
                return;
            }
            File f = file.toFile();
            if (!f.exists()) return;
            loader.loadFile(id, f);
            if (plugin.getSettings().isDebug()) {
                plugin.getLogger().info("[debug] Auto-reloaded: " + relStr);
            }
        });
    }
}
