package org.testany.fakerpp.core.store.storers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DataSources {

    private static ConcurrentMap<String, DataSource> cache =
            new ConcurrentHashMap<>();

    public static DataSource getDataSource(String jdbcUrl, String userName, String password) {
        return cache.computeIfAbsent(jdbcUrl + "_" + userName + "_" + password, ignore -> {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(userName);
            config.setPassword(password);
            return new HikariDataSource(config);
        });
    }

}
