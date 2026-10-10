package com.findit.ui.panels;

import com.findit.config.AppConfig;
import com.findit.model.Item;
import com.findit.model.MatchSuggestion;
import com.findit.model.User;
import com.findit.service.ClaimService;
import com.findit.service.ItemService;
import com.findit.service.MatchingService;
import com.findit.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Student Dashboard panel — the first screen seen after login.
 *
 * Contains:
 * - 4 stat cards (Open Lost, Open Found, My Reports, Returned)
 * - Recent items list
 * - Smart match suggestions
 */
public class DashboardPanel extends JPanel {

    private final User         user;
    private final ItemService  itemService  = new ItemService();
    private final ClaimService claimService = new ClaimService();
    private final MatchingService matchingService = new MatchingService();

    // Stat card references so we can update them after data loads
    private JLabel lostCount, foundCount, myCount, returnedCount;
    private JPanel recentPanel, matchesPanel;

    public DashboardPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout(0, 0));
        setBackground(AppConfig.BG_DARK);
        buildUI();
        loadDataAsync();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppConfig.BG_DARK);
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        // ── Greeting ────────────────────────────────────────────────────
        JLabel greeting = new JLabel("Hello, " + user.getFirstName() + "  👋");
        greeting.setFont(AppConfig.FONT_TITLE);
        greeting.setForeground(AppConfig.TEXT_PRIMARY);
        greeting.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Here's what's happening on your campus today.");
        sub.setFont(AppConfig.FONT_BODY);
        sub.setForeground(AppConfig.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(greeting);
        content.add(Box.createVerticalStrut(4));
        content.add(sub);
        content.add(Box.createVerticalStrut(24));

        // ── Stat cards row ────────────────────────────────────────────
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsRow.setOpaque(false);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardsRow.add(buildStatCard("Open Lost Items",    "🔴", AppConfig.DANGER,  n -> lostCount     = n));
        cardsRow.add(buildStatCard("Open Found Items",   "🟢", AppConfig.SUCCESS, n -> foundCount    = n));
        cardsRow.add(buildStatCard("My Active Reports",  "📋", AppConfig.PRIMARY, n -> myCount       = n));
        cardsRow.add(buildStatCard("Items Returned",     "✅", AppConfig.INFO,    n -> returnedCount = n));

        content.add(cardsRow);
        content.add(Box.createVerticalStrut(28));

        // ── Two-column lower section ──────────────────────────────────
        JPanel lower = new JPanel(new GridLayout(1, 2, 20, 0));
        lower.setOpaque(false);
        lower.setAlignmentX(Component.LEFT_ALIGNMENT);

        recentPanel  = buildSection("🕐  Recent Open Items");
        matchesPanel = buildSection("🔍  Potential Matches for You");

        lower.add(recentPanel);
        lower.add(matchesPanel);
        content.add(lower);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppConfig.BG_DARK);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        add(scroll, BorderLayout.CENTER);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    @FunctionalInterface
    interface LabelCapture { void set(JLabel label); }

    private JPanel buildStatCard(String title, String icon, Color color, LabelCapture capture) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(AppConfig.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConfig.BORDER, 1, true),
            new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setOpaque(false);

        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        JLabel ttl = new JLabel(title);
        ttl.setFont(AppConfig.FONT_SMALL);
        ttl.setForeground(AppConfig.TEXT_SECONDARY);

        top.add(ico);
        top.add(ttl);

        JLabel num = new JLabel("—");
        num.setFont(new Font("Segoe UI", Font.BOLD, 30));
        num.setForeground(color);

        capture.set(num);

        card.add(top, BorderLayout.NORTH);
        card.add(num, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildSection(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(AppConfig.BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConfig.BORDER, 1, true),
            new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppConfig.FONT_SUBHEADING);
        titleLbl.setForeground(AppConfig.TEXT_PRIMARY);
        panel.add(titleLbl, BorderLayout.NORTH);

        JLabel loading = new JLabel("Loading...");
        loading.setFont(AppConfig.FONT_BODY);
        loading.setForeground(AppConfig.TEXT_MUTED);
        loading.setBorder(new EmptyBorder(10, 0, 0, 0));
        panel.add(loading, BorderLayout.CENTER);

        return panel;
    }

    /** Loads all data in a background thread then updates the UI on the EDT. */
    private void loadDataAsync() {
        new SwingWorker<int[], List[]>() {
            int[] counts;
            List<Item> recent;
            List<MatchSuggestion> matches;

            @Override
            protected int[] doInBackground() {
                counts  = new int[]{
                    itemService.countOpenLost(),
                    itemService.countOpenFound(),
                    itemService.countMyActive(user.getUserId()),
                    itemService.countReturned()
                };
                recent  = itemService.getRecentOpen(6);
                matches = matchingService.getMatchesForUser(user.getUserId());
                return counts;
            }

            @Override
            protected void done() {
                try {
                    int[] c = get();
                    lostCount    .setText(String.valueOf(c[0]));
                    foundCount   .setText(String.valueOf(c[1]));
                    myCount      .setText(String.valueOf(c[2]));
                    returnedCount.setText(String.valueOf(c[3]));

                    populateRecentItems(recent);
                    populateMatches(matches);
                } catch (Exception e) {
                    System.err.println("Dashboard load error: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void populateRecentItems(List<Item> items) {
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        if (items.isEmpty()) {
            JLabel empty = new JLabel("No open items yet — be the first to report!");
            empty.setFont(AppConfig.FONT_BODY);
            empty.setForeground(AppConfig.TEXT_MUTED);
            list.add(empty);
        } else {
            for (Item item : items) {
                list.add(buildItemRow(item));
                list.add(Box.createVerticalStrut(6));
            }
        }
        recentPanel.remove(1); // remove Loading label
        recentPanel.add(list, BorderLayout.CENTER);
        recentPanel.revalidate();
        recentPanel.repaint();
    }

    private JPanel buildItemRow(Item item) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(AppConfig.BG_INPUT);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConfig.BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        // Left: colored dot
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(item.isLost() ? AppConfig.DANGER : AppConfig.SUCCESS);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(10, 10));
        dot.setOpaque(false);

        JPanel dotWrapper = new JPanel(new GridBagLayout());
        dotWrapper.setOpaque(false);
        dotWrapper.setPreferredSize(new Dimension(16, 16));
        dotWrapper.add(dot);

        // Center: title + location
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(item.getTitle());
        title.setFont(AppConfig.FONT_BOLD);
        title.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel loc = new JLabel("📍 " + item.getLocation() + "  •  " + DateUtil.format(item.getEventDate()));
        loc.setFont(AppConfig.FONT_SMALL);
        loc.setForeground(AppConfig.TEXT_MUTED);

        center.add(title);
        center.add(loc);

        // Right: type badge
        JLabel badge = new JLabel(item.getReportType().display());
        badge.setFont(AppConfig.FONT_SMALL);
        badge.setForeground(item.isLost() ? AppConfig.DANGER : AppConfig.SUCCESS);

        row.add(dotWrapper, BorderLayout.WEST);
        row.add(center,     BorderLayout.CENTER);
        row.add(badge,      BorderLayout.EAST);
        return row;
    }

    private void populateMatches(List<MatchSuggestion> matches) {
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        if (matches.isEmpty()) {
            JLabel empty = new JLabel("No matches found yet. Report your item first!");
            empty.setFont(AppConfig.FONT_BODY);
            empty.setForeground(AppConfig.TEXT_MUTED);
            list.add(empty);
        } else {
            for (MatchSuggestion m : matches) {
                list.add(buildMatchRow(m));
                list.add(Box.createVerticalStrut(6));
            }
        }
        matchesPanel.remove(1);
        matchesPanel.add(list, BorderLayout.CENTER);
        matchesPanel.revalidate();
        matchesPanel.repaint();
    }

    private JPanel buildMatchRow(MatchSuggestion m) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(new Color(108, 99, 255, 15));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(108, 99, 255, 60), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Lost: " + m.getLostItemTitle() + "  →  Found: " + m.getFoundItemTitle());
        title.setFont(AppConfig.FONT_BOLD);
        title.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel reasons = new JLabel(m.getMatchingReasons().length() > 60
            ? m.getMatchingReasons().substring(0, 60) + "…"
            : m.getMatchingReasons());
        reasons.setFont(AppConfig.FONT_SMALL);
        reasons.setForeground(AppConfig.TEXT_MUTED);

        center.add(title);
        center.add(reasons);

        JLabel score = new JLabel(m.getMatchScore() + "%");
        score.setFont(AppConfig.FONT_BOLD);
        score.setForeground(AppConfig.PRIMARY_LIGHT);

        row.add(center, BorderLayout.CENTER);
        row.add(score,  BorderLayout.EAST);
        return row;
    }
}
