package com.aquaticstudios.aqualang.library;

import com.aquaticstudios.aqualang.database.DatabaseType;
import revxrsal.zapper.Dependency;
import revxrsal.zapper.relocation.Relocation;

import java.util.Arrays;
import java.util.List;

public enum Libraries {

    SLF4J_API(
            null,
            new Dependency("org.slf4j", "slf4j-api", "1.7.36")
    ),

    SLF4J_SIMPLE(
            null,
            new Dependency("org.slf4j", "slf4j-simple", "1.7.36")
    ),

    MYSQL(
            DatabaseType.MYSQL,
            new Dependency("com.mysql", "mysql-connector-j", "9.2.0"),
            relocate("com{}mysql", "com.aquaticstudios.aqualang.libs.mysql")
    ),

    MARIADB(
            DatabaseType.MARIADB,
            new Dependency("org.mariadb.jdbc", "mariadb-java-client", "3.4.1"),
            relocate("org{}mariadb", "com.aquaticstudios.aqualang.libs.mariadb")
    );

    private final DatabaseType type;
    private final Dependency dependency;
    private final List<Relocation> relocations;

    Libraries(DatabaseType type, Dependency dependency, Relocation... relocations) {
        this.type = type;
        this.dependency = dependency;
        this.relocations = Arrays.asList(relocations);
    }

    public boolean isCore() {
        return type == null;
    }

    public DatabaseType type() {
        return type;
    }

    public boolean isNeededFor(DatabaseType selected) {
        return isCore() || type == selected;
    }

    public Dependency dependency() {
        return dependency;
    }

    public List<Relocation> relocations() {
        return relocations;
    }

    private static Relocation relocate(String from, String to) {
        return new Relocation(from.replace("{}", "."), to);
    }
}
