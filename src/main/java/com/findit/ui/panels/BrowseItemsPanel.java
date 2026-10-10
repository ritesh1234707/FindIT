package com.findit.ui.panels;

import com.findit.config.AppConfig;
import com.findit.model.Item;
import com.findit.model.User;
import com.findit.service.ItemService;
import com.findit.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Browse all lost and found items with search, filter, and sorting.
 *
 * Uses JTable with a custom renderer for modern row styling.
 */
public class BrowseItemsPanel extends JPanel {

    private final User        user;
    private final ItemService itemService = new ItemService();

    private JTable         table;
    private DefaultTableModel tableModel;
    private JTextField     searchField;
    private JComboBox<String> typeFilter, categoryFilter;
    private JLabel         countLabel;

    private static final String[] COLUMNS = {
        "#", "Type", "Title", "Category", "Color", "Location", "Date", "Status", "Reporter"
    };

    public BrowseItemsPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout(0, 0));
        setBackground(AppConfig.BG_DARK);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        buildUI();
        loadData();
    }

    private void buildUI() {
        // ── Page header ───────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Browse Items");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel sub = new JLabel("All lost and found reports on campus");
        sub.setFont(AppConfig.FONT_BODY);
        sub.setForeground(AppConfig.TEXT_SECONDARY);

        JPanel titleCol = new JPanel();
        titleCol.setOpaque(false);
        titleCol.setLayout(new BoxLayout(titleCol, BoxLayout.Y_AXIS));
        titleCol.add(title);
        titleCol.add(sub);

        countLabel = new JLabel("Loading...");
        countLabel.setFont(AppConfig.FONT_SMALL);
        countLabel.setForeground(AppConfig.TEXT_MUTED);

        header.add(titleCol, BorderLayout.WEST);
        header.add(countLabel, BorderLayout.EAST);

        // ── Filter bar ────────────────────────────────────────────────
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setOpaque(false);

        searchField = styledTextField("Search items...", 200);
        typeFilter     = styledCombo(new String[]{"All Types", "LOST", "FOUND"});
        categoryFilter = styledCombo(prepend("All Categories", AppConfig.CATEGORIES));

        JButton searchBtn = primaryButton("🔍 Search");
        searchBtn.addActionListener(e -> loadData());

        JButton refreshBtn = secondaryButton("↻ Refresh");
        refreshBtn.addActionListener(e -> { searchField.setText(""); loadData(); });

        filterBar.add(searchField);
        filterBar.add(typeFilter);
        filterBar.add(categoryFilter);
        filterBar.add(searchBtn);
        filterBar.add(refreshBtn);

        // ── Table ─────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(AppConfig.FONT_BODY);
        table.setForeground(AppConfig.TEXT_PRIMARY);
        table.setBackground(AppConfig.BG_CARD);
        table.setRowHeight(40);
        table.setShowHorizontalLines(true);
        table.setGridColor(AppConfig.BORDER);
        table.setSelectionBackground(new Color(108, 99, 255, 50));
        table.setSelectionForeground(AppConfig.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Header style
        JTableHeader th = table.getTableHeader();
        th.setFont(AppConfig.FONT_BOLD);
        th.setForeground(AppConfig.TEXT_SECONDARY);
        th.setBackground(AppConfig.BG_SIDEBAR);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppConfig.BORDER));
        th.setReorderingAllowed(false);

        // Column widths
        int[] widths = {40, 65, 200, 140, 80, 160, 100, 110, 130};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Custom cell renderer for coloured type/status cells
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? new Color(108, 99, 255, 50)
                                  : (row % 2 == 0 ? AppConfig.BG_CARD : AppConfig.BG_INPUT));
                setForeground(AppConfig.TEXT_PRIMARY);
                setFont(AppConfig.FONT_BODY);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setHorizontalAlignment(col == 0 ? CENTER : LEFT);

                // Colour-code Type column
                if (col == 1) {
                    String v = String.valueOf(val);
                    setForeground("LOST".equals(v) ? AppConfig.DANGER : AppConfig.SUCCESS);
                    setFont(AppConfig.FONT_BOLD);
                }
                // Colour-code Status column
                if (col == 7) {
                    String v = String.valueOf(val);
                    setForeground(switch (v) {
                        case "Open"         -> AppConfig.SUCCESS;
                        case "Returned"     -> AppConfig.INFO;
                        case "Claim Pending" -> AppConfig.WARNING;
                        case "Claimed"      -> AppConfig.PRIMARY_LIGHT;
                        case "Closed"       -> AppConfig.DANGER;
                        default             -> AppConfig.TEXT_SECONDARY;
                    });
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppConfig.BORDER, 1, true));
        scroll.getViewport().setBackground(AppConfig.BG_CARD);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        // ── Layout ────────────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header,    BorderLayout.NORTH);
        top.add(filterBar, BorderLayout.SOUTH);

        add(top,    BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadData() {
        String search   = searchField.getText().trim();
        String type     = typeFilter.getSelectedIndex() == 0 ? "ALL" : (String) typeFilter.getSelectedItem();
        String category = categoryFilter.getSelectedIndex() == 0 ? null : (String) categoryFilter.getSelectedItem();

        new SwingWorker<List<Item>, Void>() {
            @Override protected List<Item> doInBackground() {
                return itemService.getAll(type, search, category, "ALL");
            }
            @Override protected void done() {
                try {
                    List<Item> items = get();
                    tableModel.setRowCount(0);
                    int row = 1;
                    for (Item item : items) {
                        tableModel.addRow(new Object[]{
                            row++,
                            item.getReportType().name(),
                            item.getTitle(),
                            item.getCategory(),
                            item.getColor() != null ? item.getColor() : "—",
                            item.getLocation(),
                            DateUtil.format(item.getEventDate()),
                            item.getStatus().display(),
                            item.getReporterName()
                        });
                    }
                    countLabel.setText(items.size() + " item" + (items.size() != 1 ? "s" : "") + " found");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BrowseItemsPanel.this,
                        "Failed to load items: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ── Style helpers ─────────────────────────────────────────────────────

    private JTextField styledTextField(String placeholder, int width) {
        JTextField f = new JTextField(placeholder);
        f.setFont(AppConfig.FONT_BODY);
        f.setForeground(AppConfig.TEXT_MUTED);
        f.setBackground(AppConfig.BG_INPUT);
        f.setCaretColor(AppConfig.TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConfig.BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        f.setPreferredSize(new Dimension(width, 34));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(AppConfig.TEXT_PRIMARY); }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (f.getText().isBlank()) { f.setText(placeholder); f.setForeground(AppConfig.TEXT_MUTED); }
            }
        });
        return f;
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(AppConfig.FONT_BODY);
        cb.setBackground(AppConfig.BG_INPUT);
        cb.setForeground(AppConfig.TEXT_PRIMARY);
        cb.setPreferredSize(new Dimension(150, 34));
        return cb;
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(AppConfig.FONT_BOLD);
        b.setForeground(Color.WHITE);
        b.setBackground(AppConfig.PRIMARY);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(110, 34));
        return b;
    }

    private JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(AppConfig.FONT_BODY);
        b.setForeground(AppConfig.TEXT_SECONDARY);
        b.setBackground(AppConfig.BG_INPUT);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(100, 34));
        return b;
    }

    private String[] prepend(String first, String[] rest) {
        String[] result = new String[rest.length + 1];
        result[0] = first;
        System.arraycopy(rest, 0, result, 1, rest.length);
        return result;
    }
}
