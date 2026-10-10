package com.findit.dao;

import com.findit.config.DatabaseConfig;
import com.findit.model.User;

import java.sql.*;
import java.util.Optional;

/**
 * Data Access Object — handles all SQL for the users table.
 *
 * VIVA TIP: "The DAO pattern keeps database logic completely separate
 * from business logic and UI code. It also makes it easy to swap the
 * database layer later without touching any other class."
 *
 * Key JDBC concepts shown here:
 * - PreparedStatement: prevents SQL injection by parameterising queries
 * - try-with-resources: auto-closes Connection, Statement, ResultSet
 * - RETURN_GENERATED_KEYS: retrieves the auto-increment ID after INSERT
 * - Optional<T>: null-safe return type (avoids NullPointerException)
 */
public class UserDAO {

    /**
     * Inserts a new user row and returns the generated user_id.
     */
    public int save(User user) throws SQLException {
        final String sql =
            "INSERT INTO users (full_name, email, password_hash, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getRole().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("INSERT returned no generated key.");
    }

    /**
     * Looks up a user by email. Returns Optional.empty() when not found.
     */
    public Optional<User> findByEmail(String email) throws SQLException {
        final String sql = "SELECT * FROM users WHERE email = ? AND is_active = TRUE";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    /** Returns true when the email is already taken. */
    public boolean emailExists(String email) throws SQLException {
        final String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Finds a user by primary key. */
    public Optional<User> findById(int userId) throws SQLException {
        final String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    /** Total active user count for the admin dashboard. */
    public int countAll() throws SQLException {
        final String sql = "SELECT COUNT(*) FROM users WHERE is_active = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs  = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Finds all active users (for admin & quick role switch). */
    public java.util.List<User> findAll() throws SQLException {
        final String sql = "SELECT * FROM users WHERE is_active = TRUE ORDER BY role DESC, full_name ASC";
        java.util.List<User> list = new java.util.ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    // ── Private mapper ────────────────────────────────────────────────────

    private User map(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("password_hash"),
            User.Role.valueOf(rs.getString("role")),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getBoolean("is_active")
        );
    }
}
