package com.findit.dao;

import com.findit.config.DatabaseConfig;
import com.findit.model.Claim;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO for the claims table. */
public class ClaimDAO {

    private static final String SELECT_BASE =
        "SELECT c.*, " +
        "  u.full_name AS claimant_name, " +
        "  i.title    AS item_title, " +
        "  r.full_name AS reviewer_name " +
        "FROM claims c " +
        "JOIN users u  ON c.claimant_id = u.user_id " +
        "JOIN items i  ON c.item_id = i.item_id " +
        "LEFT JOIN users r ON c.reviewed_by = r.user_id";

    public int save(Claim claim) throws SQLException {
        final String sql =
            "INSERT INTO claims (item_id, claimant_id, verification_details, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt   (1, claim.getItemId());
            stmt.setInt   (2, claim.getClaimantId());
            stmt.setString(3, claim.getVerificationDetails());
            stmt.setString(4, claim.getStatus().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("INSERT returned no generated key.");
    }

    public void updateStatus(int claimId, Claim.ClaimStatus status,
                              Integer reviewedBy, String notes) throws SQLException {
        final String sql =
            "UPDATE claims SET status = ?, reviewed_by = ?, review_notes = ? WHERE claim_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            if (reviewedBy != null) stmt.setInt(2, reviewedBy);
            else stmt.setNull(2, Types.INTEGER);
            stmt.setString(3, notes);
            stmt.setInt   (4, claimId);
            stmt.executeUpdate();
        }
    }

    public Optional<Claim> findById(int claimId) throws SQLException {
        List<Claim> r = query(SELECT_BASE + " WHERE c.claim_id = ?", List.of(claimId));
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    public List<Claim> findByClaimantId(int userId) throws SQLException {
        return query(SELECT_BASE + " WHERE c.claimant_id = ? ORDER BY c.created_at DESC",
                     List.of(userId));
    }

    public List<Claim> findPending() throws SQLException {
        return query(SELECT_BASE + " WHERE c.status = 'PENDING' ORDER BY c.created_at",
                     List.of());
    }

    public List<Claim> findAll() throws SQLException {
        return query(SELECT_BASE + " ORDER BY c.created_at DESC", List.of());
    }

    public boolean hasActiveClaim(int itemId, int claimantId) throws SQLException {
        final String sql =
            "SELECT COUNT(*) FROM claims WHERE item_id = ? AND claimant_id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, itemId);
            stmt.setInt(2, claimantId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int countPending() throws SQLException {
        final String sql = "SELECT COUNT(*) FROM claims WHERE status = 'PENDING'";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countPendingByUser(int userId) throws SQLException {
        final String sql = "SELECT COUNT(*) FROM claims WHERE claimant_id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private List<Claim> query(String sql, List<Object> params) throws SQLException {
        List<Claim> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Claim map(ResultSet rs) throws SQLException {
        Claim c = new Claim();
        c.setClaimId            (rs.getInt   ("claim_id"));
        c.setItemId             (rs.getInt   ("item_id"));
        c.setClaimantId         (rs.getInt   ("claimant_id"));
        c.setClaimantName       (rs.getString("claimant_name"));
        c.setItemTitle          (rs.getString("item_title"));
        c.setVerificationDetails(rs.getString("verification_details"));
        c.setStatus             (Claim.ClaimStatus.valueOf(rs.getString("status")));

        int rev = rs.getInt("reviewed_by");
        if (!rs.wasNull()) c.setReviewedBy(rev);
        c.setReviewerName (rs.getString("reviewer_name"));
        c.setReviewNotes  (rs.getString("review_notes"));

        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) c.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) c.setUpdatedAt(ua.toLocalDateTime());

        return c;
    }
}
