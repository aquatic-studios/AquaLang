package com.aquaticstudios.aqualang.database.type;

import com.aquaticstudios.aqualang.AquaLang;
import com.aquaticstudios.aqualang.database.Database;
import com.aquaticstudios.aqualang.database.PoolSettings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public final class PostgreSQL implements Database {

    private final AquaLang plugin;
    private final String host;
    private final String database;
    private final String username;
    private final String password;
    private final int port;
    private final PoolSettings pool;

    private HikariDataSource dataSource;

    public PostgreSQL(AquaLang plugin, String host, int port, String database, String username, String password, PoolSettings pool) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.pool = pool;
    }

    @Override
    public void connect() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database);
        config.setDriverClassName("com.aquaticstudios.aqualang.libs.postgresql.Driver");
        config.setUsername(username);
        config.setPassword(password);

        config.setPoolName("AquaLang-PostgreSQL");
        pool.applyTo(config);

        dataSource = new HikariDataSource(config);
        plugin.getLogger().info("Connected to PostgreSQL.");
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
            plugin.getLogger().severe("PostgreSQL table creation failed: " + e.getMessage());
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
                "INSERT INTO AquaLang (uuid) VALUES (?) ON CONFLICT (uuid) DO NOTHING")) {
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
