package com.findit.service;

import com.findit.dao.ItemDAO;
import com.findit.dao.MatchDAO;
import com.findit.model.Item;
import com.findit.model.MatchSuggestion;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Smart Matching Algorithm for FindIT.
 *
 * ─────────────────────────────────────────────────────────────────────────
 * HOW IT WORKS (explain this in your VIVA):
 *
 * We compare every open LOST report against every open FOUND report using
 * five weighted factors:
 *
 *   Factor             Weight   Why this weight?
 *   ─────────────────────────────────────────────────────────────
 *   Category match       35 %   A lost phone will NEVER match a found wallet.
 *   Name similarity      25 %   Keyword overlap catches "JBL earbuds" vs "JBL wireless earbuds".
 *   Color match          15 %   Color narrows it down significantly.
 *   Location similarity  15 %   "Library" vs "near library" → likely same spot.
 *   Date proximity       10 %   The find date should be close to the loss date.
 *
 * The final score (0–100) is a HEURISTIC — a smart guess, not a guarantee.
 * Scores ≥ 30 are saved to the database as suggestions.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class MatchingService {

    private final ItemDAO itemDAO = new ItemDAO();
    private final MatchDAO matchDAO = new MatchDAO();

    // Weights — must sum to 100
    private static final int W_CATEGORY = 35;
    private static final int W_NAME     = 25;
    private static final int W_COLOR    = 15;
    private static final int W_LOCATION = 15;
    private static final int W_DATE     = 10;

    private static final int MIN_SCORE = 30; // Below this, don't bother saving

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Called after a new item is reported.
     * Compares it against all open items of the opposite type.
     */
    public void generateMatchesForItem(Item newItem) {
        try {
            Item.ReportType opposite = newItem.isLost() ? Item.ReportType.FOUND : Item.ReportType.LOST;
            List<Item> candidates = itemDAO.findOpenByType(opposite);

            for (Item candidate : candidates) {
                Item lost  = newItem.isLost()  ? newItem : candidate;
                Item found = newItem.isFound() ? newItem : candidate;

                MatchResult result = score(lost, found);
                if (result.score >= MIN_SCORE) {
                    matchDAO.saveOrUpdate(
                        new MatchSuggestion(lost.getItemId(), found.getItemId(),
                                            result.score, result.reasons));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MatchingService] Error: " + e.getMessage());
        }
    }

    /** Re-runs matching across ALL open items (e.g. called from Admin panel). */
    public void runFullPass() {
        try {
            List<Item> lostItems  = itemDAO.findOpenByType(Item.ReportType.LOST);
            List<Item> foundItems = itemDAO.findOpenByType(Item.ReportType.FOUND);

            for (Item lost : lostItems) {
                for (Item found : foundItems) {
                    MatchResult r = score(lost, found);
                    if (r.score >= MIN_SCORE) {
                        matchDAO.saveOrUpdate(
                            new MatchSuggestion(lost.getItemId(), found.getItemId(),
                                                r.score, r.reasons));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[MatchingService] Full-pass error: " + e.getMessage());
        }
    }

    public List<MatchSuggestion> getMatchesForUser(int userId) {
        try { return matchDAO.findForUser(userId); }
        catch (SQLException e) { return Collections.emptyList(); }
    }

    public List<MatchSuggestion> getTopMatches(int limit) {
        try { return matchDAO.findTop(limit); }
        catch (SQLException e) { return Collections.emptyList(); }
    }

    // ── Core scoring logic (public for unit testing) ──────────────────────

    public MatchResult score(Item lost, Item found) {
        StringBuilder reasons = new StringBuilder();
        int total = 0;

        // 1 ── Category (35 pts)
        int catScore = categoryScore(lost.getCategory(), found.getCategory());
        total += catScore;
        reasons.append(catScore >= W_CATEGORY ? "✓ Same category  " :
                        catScore > 0           ? "~ Similar category  " :
                                                 "✗ Different category  ");

        // 2 ── Name / keywords (25 pts)
        int nameScore = nameScore(lost.getTitle(), found.getTitle());
        total += nameScore;
        reasons.append(nameScore >= 18 ? "✓ Strong name match  " :
                        nameScore >= 8  ? "~ Partial name match  " :
                                          "✗ Different names  ");

        // 3 ── Color (15 pts)
        int colorScore = colorScore(lost.getColor(), found.getColor());
        total += colorScore;
        reasons.append(colorScore >= W_COLOR ? "✓ Same color  " :
                        colorScore > 0         ? "~ Color unknown  " :
                                                 "✗ Different color  ");

        // 4 ── Location (15 pts)
        int locScore = locationScore(lost.getLocation(), found.getLocation());
        total += locScore;
        reasons.append(locScore >= 10 ? "✓ Same/nearby location  " :
                        locScore >= 4  ? "~ Nearby location  " :
                                         "✗ Different location  ");

        // 5 ── Date proximity (10 pts)
        int dateScore = dateScore(lost.getEventDate(), found.getEventDate());
        total += dateScore;
        reasons.append(dateScore >= 8 ? "✓ Dates close  " :
                        dateScore >= 4 ? "~ Dates somewhat close  " :
                                         "✗ Dates far apart  ");

        return new MatchResult(Math.min(total, 100), reasons.toString().trim());
    }

    // ── Individual scoring methods ────────────────────────────────────────

    private int categoryScore(String c1, String c2) {
        if (c1 == null || c2 == null) return 10; // partial when unknown
        return c1.equalsIgnoreCase(c2) ? W_CATEGORY : 0;
    }

    private int nameScore(String t1, String t2) {
        if (t1 == null || t2 == null) return 0;
        Set<String> w1 = tokenize(t1), w2 = tokenize(t2);
        if (w1.isEmpty() || w2.isEmpty()) return 0;
        Set<String> inter = new HashSet<>(w1); inter.retainAll(w2);
        Set<String> union = new HashSet<>(w1); union.addAll(w2);
        double jaccard = (double) inter.size() / union.size();
        return (int) (jaccard * W_NAME);
    }

    private int colorScore(String c1, String c2) {
        boolean missing1 = c1 == null || c1.isBlank() || c1.startsWith("Select");
        boolean missing2 = c2 == null || c2.isBlank() || c2.startsWith("Select");
        if (missing1 || missing2) return 7; // partial when either color is unknown
        return c1.equalsIgnoreCase(c2) ? W_COLOR : 0;
    }

    private int locationScore(String l1, String l2) {
        if (l1 == null || l2 == null) return 0;
        Set<String> w1 = tokenize(l1), w2 = tokenize(l2);
        if (w1.isEmpty() || w2.isEmpty()) return 0;
        Set<String> inter = new HashSet<>(w1); inter.retainAll(w2);
        if (inter.isEmpty()) return 0;
        Set<String> union = new HashSet<>(w1); union.addAll(w2);
        return (int) ((double) inter.size() / union.size() * W_LOCATION);
    }

    private int dateScore(LocalDate d1, LocalDate d2) {
        if (d1 == null || d2 == null) return 5; // partial when unknown
        long diff = Math.abs(d1.toEpochDay() - d2.toEpochDay());
        if (diff <= 1)  return W_DATE;
        if (diff <= 3)  return 7;
        if (diff <= 7)  return 4;
        if (diff <= 14) return 2;
        return 0;
    }

    /**
     * Tokenises text into meaningful lowercase words,
     * filtering stop-words and very short tokens.
     */
    private Set<String> tokenize(String text) {
        if (text == null) return Collections.emptySet();
        Set<String> stop = Set.of("the","a","an","in","on","at","of","my","i",
                                   "it","and","or","near","by","from","to","is","was");
        Set<String> tokens = new LinkedHashSet<>();
        for (String raw : text.toLowerCase().split("[\\s,./;:!?()\\[\\]]+")) {
            String w = raw.replaceAll("[^a-z0-9]", "");
            if (w.length() >= 2 && !stop.contains(w)) tokens.add(w);
        }
        return tokens;
    }

    // ── Inner result type ─────────────────────────────────────────────────

    /** Holds the score and human-readable reasons string from one comparison. */
    public static class MatchResult {
        public final int    score;
        public final String reasons;
        public MatchResult(int score, String reasons) {
            this.score   = score;
            this.reasons = reasons;
        }
    }
}
