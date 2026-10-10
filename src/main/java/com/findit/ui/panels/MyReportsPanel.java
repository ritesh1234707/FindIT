package com.findit.ui.panels;

import com.findit.config.AppConfig;
import com.findit.model.Item;
import com.findit.model.User;
import com.findit.service.ClaimService;
import com.findit.service.ItemService;
import com.findit.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Shows the current user's own reports with options to close them.
 */
public class MyReportsPanel extends JPanel {

    private final User         user;
    private final ItemService  itemService  = new ItemService();
    private final ClaimService claimService = new ClaimService();

    private DefaultTableModel tableModel;
    private JTable            table;
    private List<Item>        currentItems;

    public MyReportsPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout(0, 16));
        setBackground(AppConfig.BG_DARK);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Header
        JLabel title = new JLabel("📋  My Reports");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel sub = new JLabel("All items you have reported as lost or found.");
        sub.setFont(AppConfig.FONT_BODY);
        sub.setForeground(AppConfig.TEXT_SECONDARY);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        // Table
        String[] cols = {"#", "Type", "Title", "Category", "Location", "Date", "Status", "Action"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 7; }
        };

        table = new JTable(tableModel);
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

        // Action button column renderer + editor
        table.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox(), this));

        // Column widths
        int[] widths = {40, 65, 200, 140, 160, 100, 120, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

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
                if (col == 1) setForeground("LOST".equals(val) ? AppConfig.DANGER : AppConfig.SUCCESS);
                return this;
            }
        });
        // Re-apply button column after default renderer override
        table.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox(), this));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppConfig.BORDER, 1, true));
        scroll.getViewport().setBackground(AppConfig.BG_CARD);

        add(header, BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
    }

    void loadData() {
        new SwingWorker<List<Item>, Void>() {
            @Override protected List<Item> doInBackground() {
                return itemService.getMyReports(user.getUserId());
            }
            @Override protected void done() {
                try {
                    currentItems = get();
                    tableModel.setRowCount(0);
                    int i = 1;
                    for (Item item : currentItems) {
                        tableModel.addRow(new Object[]{
                            i++,
                            item.getReportType().name(),
                            item.getTitle(),
                            item.getCategory(),
                            item.getLocation(),
                            DateUtil.format(item.getEventDate()),
                            item.getStatus().display(),
                            "Close"
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyReportsPanel.this,
                        "Error loading reports: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    void closeItem(int row) {
        if (currentItems == null || row >= currentItems.size()) return;
        Item item = currentItems.get(row);
        if (item.getStatus() != Item.ItemStatus.OPEN) {
            JOptionPane.showMessageDialog(this, "Only OPEN items can be closed.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "Mark \"" + item.getTitle() + "\" as Closed?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            itemService.updateStatus(item.getItemId(), Item.ItemStatus.CLOSED);
            loadData();
        }
    }

    // ── Button cell renderer ─────────────────────────────────────────────

    static class ButtonRenderer extends JButton implements TableCellRenderer {
        ButtonRenderer() {
            setOpaque(true);
            setFont(AppConfig.FONT_SMALL);
            setForeground(AppConfig.TEXT_SECONDARY);
            setBackground(AppConfig.BG_INPUT);
            setBorderPainted(false);
            setFocusPainted(false);
        }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            setText(val != null ? val.toString() : "");
            return this;
        }
    }

    static class ButtonEditor extends DefaultCellEditor {
        private String label;
        private final JButton btn;
        private final MyReportsPanel panel;
        private int editingRow;

        ButtonEditor(JCheckBox cb, MyReportsPanel panel) {
            super(cb);
            this.panel = panel;
            btn = new JButton();
            btn.setFont(AppConfig.FONT_SMALL);
            btn.setForeground(AppConfig.DANGER);
            btn.setBackground(AppConfig.BG_INPUT);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.addActionListener(e -> { panel.closeItem(editingRow); fireEditingStopped(); });
        }

        @Override public Component getTableCellEditorComponent(
                JTable t, Object val, boolean sel, int row, int col) {
            label = val != null ? val.toString() : "";
            editingRow = row;
            btn.setText(label);
            return btn;
        }
        @Override public Object getCellEditorValue() { return label; }
    }
}
