package com.findit.dao;

import com.findit.config.DatabaseConfig;
import com.findit.model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for the items table (lost and found reports).
 *
 * The SELECT_BASE joins users so we get the reporter's name in one query
 * instead of making a second round-trip to the database.
 */
public class ItemDAO {

    private static final String SELECT_BASE =
        "SELECT i.*, u.full_name AS reporter_name " +
        "FROM items i JOIN users u ON i.user_id = u.user_id";

    // ── Write operations ──────────────────────────────────────────────────

    public int save(Item item) throws SQLException {
        final String sql =
            "INSERT INTO items (user_id, title, category, color, brand, description, " +
            "location, event_date, report_type, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt   (1, item.getUserId());
            stmt.setString(2, item.getTitle());
            stmt.setString(3, item.getCategory());
            stmt.setString(4, item.getColor());
            stmt.setString(5, item.getBrand());
            stmt.setString(6, item.getDescription());
            stmt.setString(7, item.getLocation());
            stmt.setDate  (8, Date.valueOf(item.getEventDate()));
            stmt.setString(9, item.getReportType().name());
            stmt.setString(10, item.getStatus().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("INSERT returned no generated key.");
    }

    public void updateStatus(int itemId, Item.ItemStatus newStatus) throws SQLException {
        final String sql = "UPDATE items SET status = ? WHERE item_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setInt   (2, itemId);
            stmt.executeUpdate();
        }
    }

    // ── Read operations ───────────────────────────────────────────────────

    /**
     * Flexible search with optional filters.
     * All parameters are optional — pass null or "ALL" to skip a filter.
     */
    public List<Item> findAll(String reportType, String search,
                               String category, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (reportType != null && !reportType.equals("ALL")) {
            sql.append(" AND i.report_type = ?");
            params.add(reportType);
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (i.title LIKE ? OR i.description LIKE ? OR i.location LIKE ?)");
            String like = "%" + search + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (category != null && !category.isBlank() && !category.startsWith("All")) {
            sql.append(" AND i.category = ?");
            params.add(category);
        }
        if (status != null && !status.equals("ALL")) {
            sql.append(" AND i.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY i.created_at DESC LIMIT 200");
        return query(sql.toString(), params);
    }

    public List<Item> findByUserId(int userId) throws SQLException {
        return query(SELECT_BASE + " WHERE i.user_id = ? ORDER BY i.created_at DESC",
                     List.of(userId));
    }

    public Optional<Item> findById(int itemId) throws SQLException {
        List<Item> r = query(SELECT_BASE + " WHERE i.item_id = ?", List.of(itemId));
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    public List<Item> findOpenByType(Item.ReportType type) throws SQLException {
        return query(SELECT_BASE + " WHERE i.report_type = ? AND i.status = 'OPEN'",
                     List.of(type.name()));
    }

    public List<Item> findRecentOpen(int limit) throws SQLException {
        return query(SELECT_BASE + " WHERE i.status = 'OPEN' ORDER BY i.created_at DESC LIMIT " + limit,
                     List.of());
    }

    // ── Count helpers for dashboard stats ────────────────────────────────

    public int countOpenByType(Item.ReportType type) throws SQLException {
        return count("SELECT COUNT(*) FROM items WHERE report_type = ? AND status = 'OPEN'",
                     List.of(type.name()));
    }

    public int countByStatus(Item.ItemStatus status) throws SQLException {
        return count("SELECT COUNT(*) FROM items WHERE status = ?", List.of(status.name()));
    }

    public int countActiveByUser(int userId) throws SQLException {
        return count("SELECT COUNT(*) FROM items WHERE user_id = ? AND status IN ('OPEN','CLAIM_PENDING')",
                     List.of(userId));
    }

    public int countTotal() throws SQLException {
        return count("SELECT COUNT(*) FROM items", List.of());
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private List<Item> query(String sql, List<Object> params) throws SQLException {
        List<Item> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private int count(String sql, List<Object> params) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Item map(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setItemId      (rs.getInt   ("item_id"));
        item.setUserId      (rs.getInt   ("user_id"));
        item.setReporterName(rs.getString("reporter_name"));
        item.setTitle       (rs.getString("title"));
        item.setCategory    (rs.getString("category"));
        item.setColor       (rs.getString("color"));
        item.setBrand       (rs.getString("brand"));
        item.setDescription (rs.getString("description"));
        item.setLocation    (rs.getString("location"));

        Date d = rs.getDate("event_date");
        if (d != null) item.setEventDate(d.toLocalDate());

        item.setReportType(Item.ReportType.valueOf(rs.getString("report_type")));
        item.setStatus    (Item.ItemStatus.valueOf(rs.getString("status")));

        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) item.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) item.setUpdatedAt(ua.toLocalDateTime());

        return item;
    }
}
