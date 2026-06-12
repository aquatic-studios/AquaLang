package com.aquaticstudios.aqualang.addon.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class Scheduler {

    private static final boolean FOLIA;

    private static Method getScheduler;
    private static Method entityRunDelayed;

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
                getScheduler = Entity.class.getMethod("getScheduler");
                Class<?> entityScheduler = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
                entityRunDelayed = entityScheduler.getMethod("runDelayed",
                        Plugin.class, Consumer.class, Runnable.class, long.class);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Folia detected but its entity scheduler could not be loaded", e);
            }
        }
    }

    private Scheduler() {}

    public static void atEntityLater(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        if (FOLIA) {
            try {
                Object scheduler = getScheduler.invoke(entity);
                Consumer<Object> consumer = ignored -> task.run();
                entityRunDelayed.invoke(scheduler, plugin, consumer, null, delayTicks);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Folia entity scheduler invocation failed", e);
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }
}
