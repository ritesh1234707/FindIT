package com.findit.ui.panels;

import com.findit.config.AppConfig;
import com.findit.model.Claim;
import com.findit.model.User;
import com.findit.service.ClaimService;
import com.findit.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/** Shows claims submitted by the current student. */
public class MyClaimsPanel extends JPanel {

    private final User         user;
    private final ClaimService claimService = new ClaimService();

    private DefaultTableModel tableModel;

    public MyClaimsPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout(0, 16));
        setBackground(AppConfig.BG_DARK);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JLabel title = new JLabel("✅  My Claims");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel sub = new JLabel("Track the status of your ownership claims.");
        sub.setFont(AppConfig.FONT_BODY);
        sub.setForeground(AppConfig.TEXT_SECONDARY);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        String[] cols = {"#", "Item", "Submitted", "Status", "Reviewer", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setFont(AppConfig.FONT_BODY);
        table.setForeground(AppConfig.TEXT_PRIMARY);
        table.setBackground(AppConfig.BG_CARD);
        table.setRowHeight(40);
        table.setShowHorizontalLines(true);
        table.setGridColor(AppConfig.BORDER);
        table.setSelectionBackground(new Color(108, 99, 255, 50));
        table.setFillsViewportHeight(true);

        JTableHeader th = table.getTableHeader();
        th.setFont(AppConfig.FONT_BOLD);
        th.setForeground(AppConfig.TEXT_SECONDARY);
        th.setBackground(AppConfig.BG_SIDEBAR);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppConfig.BORDER));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? new Color(108, 99, 255, 50)
                                  : (row % 2 == 0 ? AppConfig.BG_CARD : AppConfig.BG_INPUT));
                setForeground(AppConfig.TEXT_PRIMARY);
                setFont(AppConfig.FONT_BODY);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                // Colour the status column
                if (col == 3) {
                    String s = String.valueOf(val);
                    setForeground(switch (s) {
                        case "Approved"      -> AppConfig.SUCCESS;
                        case "Rejected"      -> AppConfig.DANGER;
                        case "Pending Review"-> AppConfig.WARNING;
                        default              -> AppConfig.TEXT_SECONDARY;
                    });
                    setFont(AppConfig.FONT_BOLD);
                }
                return this;
            }
        });

        int[] widths = {40, 220, 120, 130, 130, 260};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppConfig.BORDER, 1, true));
        scroll.getViewport().setBackground(AppConfig.BG_CARD);

        add(header, BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
    }

    private void loadData() {
        new SwingWorker<List<Claim>, Void>() {
            @Override protected List<Claim> doInBackground() {
                return claimService.getMyClaims(user.getUserId());
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    int i = 1;
                    for (Claim c : get()) {
                        tableModel.addRow(new Object[]{
                            i++,
                            c.getItemTitle(),
                            DateUtil.format(c.getCreatedAt()),
                            c.getStatus().display(),
                            c.getReviewerName() != null ? c.getReviewerName() : "Pending",
                            c.getReviewNotes()  != null ? c.getReviewNotes()  : "—"
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyClaimsPanel.this,
                        "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
