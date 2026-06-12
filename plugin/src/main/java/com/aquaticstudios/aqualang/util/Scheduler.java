package com.aquaticstudios.aqualang.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class Scheduler {

    private static final boolean FOLIA;

    private static Object asyncScheduler;
    private static Object globalScheduler;
    private static Method asyncRunNow;
    private static Method globalRun;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException ignored) {
            folia = false;
        }
        FOLIA = folia;

        if (FOLIA) {
            try {
                asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
                globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);

                Class<?> asyncType = Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
                Class<?> globalType = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");

                asyncRunNow = asyncType.getMethod("runNow", Plugin.class, Consumer.class);
                globalRun = globalType.getMethod("run", Plugin.class, Consumer.class);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Folia detected but its schedulers could not be loaded", e);
            }
        }
    }

    private Scheduler() {}

    public static boolean isFolia() {
        return FOLIA;
    }

    public static void async(Plugin plugin, Runnable task) {
        if (FOLIA) {
            invoke(asyncRunNow, asyncScheduler, plugin, task);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public static void global(Plugin plugin, Runnable task) {
        if (FOLIA) {
            invoke(globalRun, globalScheduler, plugin, task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    private static void invoke(Method method, Object scheduler, Plugin plugin, Runnable task) {
        try {
            Consumer<Object> consumer = ignored -> task.run();
            method.invoke(scheduler, plugin, consumer);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Folia scheduler invocation failed", e);
        }
    }
}
