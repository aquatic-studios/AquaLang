package com.aquaticstudios.aqualang.database;

import com.aquaticstudios.aqualang.AquaLang;
import com.aquaticstudios.aqualang.database.type.MariaDB;
import com.aquaticstudios.aqualang.database.type.MySQL;
import com.aquaticstudios.aqualang.database.type.SQLite;
import org.bukkit.configuration.ConfigurationSection;

public final class DatabaseManager {

    private static Database database;

    private DatabaseManager() {}

    public static void loadDatabase() {
        AquaLang plugin = AquaLang.getInstance();
        String typeName = plugin.getMainConfig().getString("database.type", "SQLITE");

        DatabaseType type;
        try {
            type = DatabaseType.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid database.type, using SQLite.");
            type = DatabaseType.SQLITE;
        }

        PoolSettings pool = new PoolSettings(
                plugin.getMainConfig().getConfigurationSection("database.pool"));

        switch (type) {
            case MYSQL:
            case MARIADB:
                database = loadRemote(plugin, type, pool);
                break;
            case SQLITE:
                database = new SQLite(plugin, pool);
                break;
        }

        database.connect();
        database.load();
    }

    private static Database loadRemote(AquaLang plugin, DatabaseType type, PoolSettings pool) {
        ConfigurationSection config = plugin.getMainConfig().getConfigurationSection("database");

        if (config == null) {
            plugin.getLogger().severe("Missing database section, falling back to SQLite.");
            return new SQLite(plugin, pool);
        }

        String host = config.getString("address", "127.0.0.1");
        int port = config.getInt("port", 3306);
        String database = config.getString("database", "AquaLang");
        String username = config.getString("username", "root");
        String password = config.getString("password", "");

        if (type == DatabaseType.MARIADB) {
            return new MariaDB(plugin, host, port, database, username, password, pool);
        }
        return new MySQL(plugin, host, port, database, username, password, pool);
    }

    public static Database getDatabase() {
        return database;
    }

    public static void close() {
        if (database != null) {
            database.close();
        }
    }
}
