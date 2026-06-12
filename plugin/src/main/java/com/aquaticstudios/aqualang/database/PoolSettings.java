package com.aquaticstudios.aqualang.database;

import com.zaxxer.hikari.HikariConfig;
import org.bukkit.configuration.ConfigurationSection;

public final class PoolSettings {

    private final int maximumPoolSize;
    private final int minimumIdle;
    private final long maximumLifetime;
    private final long keepaliveTime;
    private final long connectionTimeout;

    public PoolSettings(ConfigurationSection pool) {
        if (pool == null) {
            this.maximumPoolSize = 10;
            this.minimumIdle = 10;
            this.maximumLifetime = 1800000L;
            this.keepaliveTime = 0L;
            this.connectionTimeout = 5000L;
        } else {
            this.maximumPoolSize = pool.getInt("maximum-pool-size", 10);
            this.minimumIdle = pool.getInt("minimum-idle", 10);
            this.maximumLifetime = pool.getLong("maximum-lifetime", 1800000L);
            this.keepaliveTime = pool.getLong("keepalive-time", 0L);
            this.connectionTimeout = pool.getLong("connection-timeout", 5000L);
        }
    }

    public void applyTo(HikariConfig config) {
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setMaxLifetime(maximumLifetime);
        if (keepaliveTime > 0L) {
            config.setKeepaliveTime(keepaliveTime);
        }
        config.setConnectionTimeout(connectionTimeout);
    }

    public int maximumPoolSize() {
        return maximumPoolSize;
    }
}
