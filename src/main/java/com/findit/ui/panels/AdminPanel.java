package com.findit.ui.panels;

import com.findit.config.AppConfig;
import com.findit.model.Claim;
import com.findit.model.User;
import com.findit.service.ClaimService;
import com.findit.service.DashboardService;
import com.findit.service.ItemService;
import com.findit.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Admin-only panel for reviewing and acting on pending claims,
 * plus a live statistics overview.
 */
public class AdminPanel extends JPanel {

    private final User            admin;
    private final ClaimService    claimService    = new ClaimService();
    private final ItemService     itemService     = new ItemService();
    private final DashboardService dashService    = new DashboardService();

    private DefaultTableModel tableModel;
    private List<Claim>       pendingClaims;

    // Stat labels
    private JLabel lblUsers, lblLost, lblFound, lblPending, lblReturned, lblRate;

    public AdminPanel(User admin) {
        this.admin = admin;
        setLayout(new BorderLayout(0, 0));
        setBackground(AppConfig.BG_DARK);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // ── Header ────────────────────────────────────────────────────
        JLabel title = new JLabel("⚙️  Admin Dashboard");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Review claims, manage reports, and monitor recovery statistics.");
        sub.setFont(AppConfig.FONT_BODY);
        sub.setForeground(AppConfig.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(sub);
        content.add(Box.createVerticalStrut(24));

        // ── Stat cards ────────────────────────────────────────────────
        JPanel statsRow = new JPanel(new GridLayout(1, 6, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblUsers    = addStatCard(statsRow, "Total Users",    "👥", AppConfig.INFO);
        lblLost     = addStatCard(statsRow, "Open Lost",      "🔴", AppConfig.DANGER);
        lblFound    = addStatCard(statsRow, "Open Found",     "🟢", AppConfig.SUCCESS);
        lblPending  = addStatCard(statsRow, "Pending Claims", "⏳", AppConfig.WARNING);
        lblReturned = addStatCard(statsRow, "Returned",       "✅", AppConfig.PRIMARY);
        lblRate     = addStatCard(statsRow, "Recovery Rate",  "📊", AppConfig.ACCENT);

        content.add(statsRow);
        content.add(Box.createVerticalStrut(28));

        // ── Pending claims section ────────────────────────────────────
        JLabel claimsTitle = new JLabel("⏳  Pending Claims — Requires Review");
        claimsTitle.setFont(AppConfig.FONT_HEADING);
        claimsTitle.setForeground(AppConfig.TEXT_PRIMARY);
        claimsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(claimsTitle);
        content.add(Box.createVerticalStrut(12));

        // Table
        String[] cols = {"#", "Item", "Claimant", "Submitted", "Verification Details", "Approve", "Reject"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5 || c == 6; }
        };

        JTable table = new JTable(tableModel);
        table.setFont(AppConfig.FONT_BODY);
        table.setForeground(AppConfig.TEXT_PRIMARY);
        table.setBackground(AppConfig.BG_CARD);
        table.setRowHeight(42);
        table.setShowHorizontalLines(true);
        table.setGridColor(AppConfig.BORDER);
        table.setSelectionBackground(new Color(108, 99, 255, 50));
        table.setFillsViewportHeight(true);

        JTableHeader th = table.getTableHeader();
        th.setFont(AppConfig.FONT_BOLD);
        th.setForeground(AppConfig.TEXT_SECONDARY);
        th.setBackground(AppConfig.BG_SIDEBAR);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppConfig.BORDER));

        // Row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? new Color(108, 99, 255, 50)
                                  : (row % 2 == 0 ? AppConfig.BG_CARD : AppConfig.BG_INPUT));
                setForeground(AppConfig.TEXT_PRIMARY);
                setFont(AppConfig.FONT_BODY);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        // Approve button column
        ActionButtonRenderer approveRender = new ActionButtonRenderer("✓ Approve", AppConfig.SUCCESS);
        ActionButtonEditor   approveEditor = new ActionButtonEditor(new JCheckBox(), "approve", this);
        table.getColumnModel().getColumn(5).setCellRenderer(approveRender);
        table.getColumnModel().getColumn(5).setCellEditor(approveEditor);

        // Reject button column
        ActionButtonRenderer rejectRender = new ActionButtonRenderer("✗ Reject", AppConfig.DANGER);
        ActionButtonEditor   rejectEditor = new ActionButtonEditor(new JCheckBox(), "reject", this);
        table.getColumnModel().getColumn(6).setCellRenderer(rejectRender);
        table.getColumnModel().getColumn(6).setCellEditor(rejectEditor);

        int[] widths = {40, 180, 140, 110, 280, 100, 90};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppConfig.BORDER, 1, true));
        scroll.getViewport().setBackground(AppConfig.BG_CARD);
        scroll.setPreferredSize(new Dimension(0, 300));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(scroll);

        JScrollPane outer = new JScrollPane(content);
        outer.setBorder(null);
        outer.getViewport().setBackground(AppConfig.BG_DARK);
        outer.getVerticalScrollBar().setUnitIncrement(12);
        add(outer, BorderLayout.CENTER);
    }

    private JLabel addStatCard(JPanel row, String label, String icon, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(AppConfig.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConfig.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        top.setOpaque(false);
        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppConfig.FONT_SMALL);
        lbl.setForeground(AppConfig.TEXT_SECONDARY);
        top.add(ico); top.add(lbl);

        JLabel num = new JLabel("—");
        num.setFont(new Font("Segoe UI", Font.BOLD, 24));
        num.setForeground(color);

        card.add(top, BorderLayout.NORTH);
        card.add(num, BorderLayout.CENTER);
        row.add(card);
        return num;
    }

    void loadData() {
        new SwingWorker<Void, Void>() {
            Map<String, Integer> stats;
            List<Claim> claims;
            @Override protected Void doInBackground() {
                stats  = dashService.getAdminStats();
                claims = claimService.getPendingClaims();
                stats.put("pendingClaims", claimService.countPending());
                return null;
            }
            @Override protected void done() {
                try { get(); } catch (Exception ignore) {}
                if (stats != null) {
                    lblUsers   .setText(String.valueOf(stats.getOrDefault("totalUsers",    0)));
                    lblLost    .setText(String.valueOf(stats.getOrDefault("totalLost",     0)));
                    lblFound   .setText(String.valueOf(stats.getOrDefault("totalFound",    0)));
                    lblPending .setText(String.valueOf(stats.getOrDefault("pendingClaims", 0)));
                    lblReturned.setText(String.valueOf(stats.getOrDefault("returned",      0)));
                    lblRate    .setText(stats.getOrDefault("recoveryRate", 0) + "%");
                }
                if (claims != null) {
                    pendingClaims = claims;
                    tableModel.setRowCount(0);
                    int i = 1;
                    for (Claim c : claims) {
                        tableModel.addRow(new Object[]{
                            i++,
                            c.getItemTitle(),
                            c.getClaimantName(),
                            DateUtil.format(c.getCreatedAt()),
                            c.getVerificationDetails(),
                            "Approve",
                            "Reject"
                        });
                    }
                }
            }
        }.execute();
    }

    void handleAction(int row, String action) {
        if (pendingClaims == null || row >= pendingClaims.size()) return;
        Claim claim = pendingClaims.get(row);

        String notes = JOptionPane.showInputDialog(this,
            "Add review notes (optional):", action.equals("approve") ? "Approval Notes" : "Rejection Reason");
        if (notes == null) return; // cancelled

        try {
            if ("approve".equals(action)) {
                claimService.approveClaim(claim.getClaimId(), admin.getUserId(), notes);
                JOptionPane.showMessageDialog(this, "✅ Claim approved!", "Done", JOptionPane.INFORMATION_MESSAGE);
            } else {
                claimService.rejectClaim(claim.getClaimId(), admin.getUserId(), notes);
                JOptionPane.showMessageDialog(this, "Claim rejected.", "Done", JOptionPane.INFORMATION_MESSAGE);
            }
            loadData(); // refresh
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Button cell helpers ───────────────────────────────────────────────

    static class ActionButtonRenderer extends JButton implements TableCellRenderer {
        ActionButtonRenderer(String text, Color fg) {
            setText(text);
            setFont(AppConfig.FONT_SMALL);
            setForeground(fg);
            setBackground(AppConfig.BG_INPUT);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(true);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            setBackground(sel ? AppConfig.BG_HOVER : AppConfig.BG_INPUT);
            return this;
        }
    }

    static class ActionButtonEditor extends DefaultCellEditor {
        private final String      action;
        private final AdminPanel  panel;
        private final JButton     btn;
        private int               editingRow;

        ActionButtonEditor(JCheckBox cb, String action, AdminPanel panel) {
            super(cb);
            this.action = action;
            this.panel  = panel;
            btn = new JButton(action.equals("approve") ? "✓ Approve" : "✗ Reject");
            btn.setFont(AppConfig.FONT_SMALL);
            btn.setForeground(action.equals("approve") ? AppConfig.SUCCESS : AppConfig.DANGER);
            btn.setBackground(AppConfig.BG_INPUT);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.addActionListener(e -> { panel.handleAction(editingRow, action); fireEditingStopped(); });
        }

        @Override public Component getTableCellEditorComponent(JTable t, Object val,
                boolean sel, int row, int col) {
            editingRow = row;
            return btn;
        }
        @Override public Object getCellEditorValue() { return btn.getText(); }
    }
}
