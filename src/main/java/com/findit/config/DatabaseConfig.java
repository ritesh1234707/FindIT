package com.findit.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages JDBC database connectivity for FindIT.
 *
 * Design: Utility class with static factory method for connections.
 *
 * VIVA TIP: "DatabaseConfig reads credentials from db.properties at
 * startup, so we never hardcode passwords in source code. Each call to
 * getConnection() returns a fresh connection; the caller must close it —
 * we enforce this using try-with-resources throughout the DAO layer."
 */
public class DatabaseConfig {

    private static String jdbcUrl;
    private static String username;
    private static String password;

    /**
     * Loads db.properties and tests the connection.
     * Must be called once before any DAO operations.
     */
    public static void initialize() {
        loadProperties();
        testConnection();
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private static void loadProperties() {
        Properties props = new Properties();

        // getResourceAsStream looks inside src/main/resources on the classpath
        try (InputStream input = DatabaseConfig.class
                .getClassLoader().getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException(
                    "db.properties not found!\n" +
                    "Create the file at: src/main/resources/db.properties");
            }

            props.load(input);

            jdbcUrl  = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");

            if (jdbcUrl == null || username == null || password == null) {
                throw new RuntimeException(
                    "db.properties is missing required keys: db.url, db.username, db.password");
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read db.properties: " + e.getMessage(), e);
        }
    }

    private static void testConnection() {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            if (!conn.isValid(5)) {
                throw new RuntimeException("Database connection test failed.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Cannot connect to MySQL!\n" +
                "• Is MySQL running?\n" +
                "• Are credentials in db.properties correct?\n\n" +
                "Error: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a new JDBC Connection.
     *
     * <p>Always use try-with-resources:
     * <pre>
     *   try (Connection conn = DatabaseConfig.getConnection()) {
     *       // use conn
     *   }
     * </pre>
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}
