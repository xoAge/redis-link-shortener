package io.github.xoage.shortener.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

public final class Database {

    private Database() {}

    public static HikariDataSource createMigrated(String jdbcUrl, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(5);

        HikariDataSource dataSource = new HikariDataSource(config);
        try {
            Flyway.configure().dataSource(dataSource).load().migrate();
        } catch (RuntimeException e) {
            dataSource.close();
            throw e;
        }
        return dataSource;
    }
}
