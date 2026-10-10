package com.findit.dao;

import com.findit.config.DatabaseConfig;
import com.findit.model.MatchSuggestion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO for match_suggestions table. */
public class MatchDAO {

    private static final String SELECT_BASE =
        "SELECT m.*, l.title AS lost_item_title, f.title AS found_item_title " +
        "FROM match_suggestions m " +
        "JOIN items l ON m.lost_item_id  = l.item_id " +
        "JOIN items f ON m.found_item_id = f.item_id";

    /**
     * Insert or refresh a match suggestion.
     * The UNIQUE KEY on (lost_item_id, found_item_id) means we just update
     * the score if this pair has already been suggested.
     */
    public void saveOrUpdate(MatchSuggestion match) throws SQLException {
        final String sql =
            "INSERT INTO match_suggestions (lost_item_id, found_item_id, match_score, matching_reasons) " +
            "VALUES (?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE match_score = ?, matching_reasons = ?, status = 'SUGGESTED'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt   (1, match.getLostItemId());
            stmt.setInt   (2, match.getFoundItemId());
            stmt.setInt   (3, match.getMatchScore());
            stmt.setString(4, match.getMatchingReasons());
            stmt.setInt   (5, match.getMatchScore());
            stmt.setString(6, match.getMatchingReasons());
            stmt.executeUpdate();
        }
    }

    /** Returns relevant matches for a user (where they are the reporter of either item). */
    public List<MatchSuggestion> findForUser(int userId) throws SQLException {
        final String sql = SELECT_BASE +
            " WHERE m.status = 'SUGGESTED' AND m.match_score >= 40" +
            "   AND (l.user_id = ? OR f.user_id = ?)" +
            " ORDER BY m.match_score DESC LIMIT 10";
        return query(sql, List.of(userId, userId));
    }

    /** Returns the top N matches across the whole system (admin view). */
    public List<MatchSuggestion> findTop(int limit) throws SQLException {
        final String sql = SELECT_BASE +
            " WHERE m.status = 'SUGGESTED' AND m.match_score >= 50" +
            " ORDER BY m.match_score DESC LIMIT " + limit;
        return query(sql, List.of());
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private List<MatchSuggestion> query(String sql, List<Object> params) throws SQLException {
        List<MatchSuggestion> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private MatchSuggestion map(ResultSet rs) throws SQLException {
        MatchSuggestion m = new MatchSuggestion();
        m.setMatchId        (rs.getInt   ("match_id"));
        m.setLostItemId     (rs.getInt   ("lost_item_id"));
        m.setFoundItemId    (rs.getInt   ("found_item_id"));
        m.setLostItemTitle  (rs.getString("lost_item_title"));
        m.setFoundItemTitle (rs.getString("found_item_title"));
        m.setMatchScore     (rs.getInt   ("match_score"));
        m.setMatchingReasons(rs.getString("matching_reasons"));
        m.setStatus(MatchSuggestion.MatchStatus.valueOf(rs.getString("status")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) m.setCreatedAt(ts.toLocalDateTime());
        return m;
    }
}
