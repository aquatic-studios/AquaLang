package com.aquaticstudios.aqualang.database.type;

import com.aquaticstudios.aqualang.AquaLang;
import com.aquaticstudios.aqualang.database.Database;
import com.aquaticstudios.aqualang.database.PoolSettings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public final class SQLite implements Database {

    private final AquaLang plugin;
    private final PoolSettings pool;
    private HikariDataSource dataSource;

    public SQLite(AquaLang plugin, PoolSettings pool) {
        this.plugin = plugin;
        this.pool = pool;
    }

    @Override
    public void connect() {
        File file = new File(plugin.getDataFolder(), "database.db");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
        config.setDriverClassName("com.aquaticstudios.aqualang.libs.sqlite.JDBC");
        config.setPoolName("AquaLang-SQLite");
        pool.applyTo(config);
        // SQLite allows a single writer; force one connection to avoid "database is locked".
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);

        dataSource = new HikariDataSource(config);
        plugin.getLogger().info("Connected to SQLite database.");
    }

    @Override
    public void load() {
        String sql =
                "CREATE TABLE IF NOT EXISTS AquaLang (\n" +
                "    uuid VARCHAR(36) PRIMARY KEY,\n" +
                "    language VARCHAR(64)\n" +
                ")\n";

        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("SQLite table creation failed: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private void ensurePlayer(Connection con, UUID uuid) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT OR IGNORE INTO AquaLang (uuid) VALUES (?)")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }

    @Override
    public void setLanguagePlayer(UUID uuid, String language) {
        try (Connection con = dataSource.getConnection()) {
            ensurePlayer(con, uuid);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE AquaLang SET language=? WHERE uuid=?")) {
                ps.setString(1, language);
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set language: " + e.getMessage());
        }
    }

    @Override
    public String getLanguagePlayer(UUID uuid) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT language FROM AquaLang WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("language") : null;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get language: " + e.getMessage());
            return null;
        }
    }
}
