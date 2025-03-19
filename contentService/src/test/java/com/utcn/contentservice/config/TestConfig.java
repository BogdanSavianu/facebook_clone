package com.utcn.contentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private Connection adminConnection;
    private String dbName;

    @PostConstruct
    public void setUp() {
        try {
            // Extract database name from URL
            dbName = dbUrl.substring(dbUrl.lastIndexOf("/") + 1);
            
            // Create a single admin connection to postgres database
            String adminUrl = dbUrl.substring(0, dbUrl.lastIndexOf("/")) + "/postgres";
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(adminUrl);
            config.setUsername(dbUsername);
            config.setPassword(dbPassword);
            config.setMaximumPoolSize(1);
            
            try (HikariDataSource adminDs = new HikariDataSource(config)) {
                adminConnection = adminDs.getConnection();
                
                try (Statement stmt = adminConnection.createStatement()) {
                    // Terminate existing connections to the test database
                    stmt.execute("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '" + dbName + "'");
                    // Drop database if exists and create new one
                    stmt.execute("DROP DATABASE IF EXISTS " + dbName);
                    stmt.execute("CREATE DATABASE " + dbName);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to setup test database", e);
        }
    }

    @PreDestroy
    public void tearDown() {
        try {
            // Create a new admin connection since the old one might be closed
            String adminUrl = dbUrl.substring(0, dbUrl.lastIndexOf("/")) + "/postgres";
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(adminUrl);
            config.setUsername(dbUsername);
            config.setPassword(dbPassword);
            config.setMaximumPoolSize(1);
            
            try (HikariDataSource adminDs = new HikariDataSource(config)) {
                try (Connection conn = adminDs.getConnection();
                     Statement stmt = conn.createStatement()) {
                    // Terminate existing connections to the test database
                    stmt.execute("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '" + dbName + "'");
                    // Drop test database
                    stmt.execute("DROP DATABASE IF EXISTS " + dbName);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to cleanup test database", e);
        }
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getUrl());
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(1);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(20000);
        
        return new HikariDataSource(config);
    }
} 