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
        // 1. First check environment variables (ideal for Cloud / Render / Heroku)
        String envDbUrl = System.getenv("DB_URL");
        String envUser  = System.getenv("DB_USERNAME");
        String envPass  = System.getenv("DB_PASSWORD");
        String envUri   = System.getenv("DATABASE_URL");

        if (envUri != null && !envUri.isBlank()) {
            parseUri(envUri.trim());
            return;
        }

        if (envDbUrl != null && !envDbUrl.isBlank()) {
            jdbcUrl  = envDbUrl.trim();
            username = envUser != null ? envUser.trim() : "";
            password = envPass != null ? envPass.trim() : "";
            return;
        }

        // 2. Fall back to local db.properties
        Properties props = new Properties();
        try (InputStream input = DatabaseConfig.class
                .getClassLoader().getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException(
                    "db.properties not found and no DB environment variables set!\n" +
                    "Set DB_URL, DB_USERNAME, DB_PASSWORD or create src/main/resources/db.properties");
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

    private static void parseUri(String uri) {
        // Handles mysql://username:password@host:port/database format
        try {
            if (uri.startsWith("jdbc:")) {
                jdbcUrl = uri;
                username = System.getenv("DB_USERNAME");
                password = System.getenv("DB_PASSWORD");
                return;
            }

            java.net.URI parsed = java.net.URI.create(uri.replace("mysql://", "http://"));
            String host = parsed.getHost();
            int port = parsed.getPort() > 0 ? parsed.getPort() : 3306;
            String path = parsed.getPath();
            if (path.startsWith("/")) path = path.substring(1);
            String dbName = path.isEmpty() ? "findit_db" : path;

            String userInfo = parsed.getUserInfo();
            if (userInfo != null && userInfo.contains(":")) {
                String[] parts = userInfo.split(":", 2);
                username = java.net.URLDecoder.decode(parts[0], java.nio.charset.StandardCharsets.UTF_8);
                password = java.net.URLDecoder.decode(parts[1], java.nio.charset.StandardCharsets.UTF_8);
            }

            String query = parsed.getQuery();
            String params = query != null ? "?" + query : "?useSSL=true&allowPublicKeyRetrieval=true";
            if (!params.contains("allowPublicKeyRetrieval")) {
                params += "&allowPublicKeyRetrieval=true";
            }

            jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName + params;
        } catch (Exception e) {
            System.err.println("Warning: Could not parse DATABASE_URL as URI, using as raw JDBC: " + e.getMessage());
            jdbcUrl = uri;
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
