package com.findit.ui.panels;

import com.findit.config.AppConfig;
import com.findit.model.Item;
import com.findit.model.User;
import com.findit.service.ItemService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

/**
 * Form panel for reporting a LOST or FOUND item.
 *
 * Reused for both types — pass the report type in the constructor.
 */
public class ReportItemPanel extends JPanel {

    private final User            user;
    private final Item.ReportType reportType;
    private final ItemService     itemService = new ItemService();

    private JTextField titleField, brandField, locationField;
    private JComboBox<String> categoryBox, colorBox;
    private JTextArea descriptionArea;
    private JSpinner  dateSpinner;
    private JLabel    statusLabel;

    public ReportItemPanel(User user, Item.ReportType type) {
        this.user       = user;
        this.reportType = type;
        setLayout(new BorderLayout());
        setBackground(AppConfig.BG_DARK);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        buildUI();
    }

    private void buildUI() {
        boolean isLost = reportType == Item.ReportType.LOST;
        String emoji  = isLost ? "📝" : "📦";
        String label  = isLost ? "Report Lost Item" : "Report Found Item";

        // ── Header ────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(emoji + "  " + label);
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.TEXT_PRIMARY);

        JLabel sub = new JLabel(isLost
            ? "Fill in the details below. The more info you give, the better the matching."
            : "Describe what you found so the owner can identify it.");
        sub.setFont(AppConfig.FONT_BODY);
        sub.setForeground(AppConfig.TEXT_SECONDARY);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        // ── Form card ─────────────────────────────────────────────────
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(AppConfig.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConfig.BORDER, 1, true),
            new EmptyBorder(28, 28, 28, 28)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Row 1: Title
        addLabel(card, gbc, row, 0, "Item Title *");
        titleField = addTextField(card, gbc, row, 1, "e.g. Black JBL Earbuds", 2);
        row++;

        // Row 2: Category + Color
        addLabel(card, gbc, row, 0, "Category *");
        categoryBox = addCombo(card, gbc, row, 1, AppConfig.CATEGORIES);
        addLabel(card, gbc, row, 2, "Color");
        colorBox = addCombo(card, gbc, row, 3, AppConfig.COLORS);
        row++;

        // Row 3: Brand + Location
        addLabel(card, gbc, row, 0, "Brand");
        brandField = addTextField(card, gbc, row, 1, "e.g. Samsung, Casio", 1);
        addLabel(card, gbc, row, 2, "Location *");
        locationField = addTextField(card, gbc, row, 3, "e.g. Library, Canteen, Block A", 1);
        row++;

        // Row 4: Date
        addLabel(card, gbc, row, 0, (isLost ? "Date Lost *" : "Date Found *"));
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        styleSpinner(dateSpinner);
        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 1;
        card.add(dateSpinner, gbc);
        row++;

        // Row 5: Description
        addLabel(card, gbc, row, 0, "Description");
        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 3; gbc.weighty = 1.0;
        descriptionArea = new JTextArea(4, 30);
        descriptionArea.setFont(AppConfig.FONT_BODY);
        descriptionArea.setForeground(AppConfig.TEXT_PRIMARY);
        descriptionArea.setBackground(AppConfig.BG_INPUT);
        descriptionArea.setCaretColor(AppConfig.TEXT_PRIMARY);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConfig.BORDER),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        descriptionArea.setText(isLost
            ? "Describe the item in detail — distinctive markings, what was inside, etc."
            : "Describe where exactly you found it and what condition it's in.");
        descriptionArea.setForeground(AppConfig.TEXT_MUTED);
        descriptionArea.addFocusListener(new java.awt.event.FocusAdapter() {
            String hint = descriptionArea.getText();
            public void focusGained(java.awt.event.FocusEvent e) {
                if (descriptionArea.getText().equals(hint)) {
                    descriptionArea.setText("");
                    descriptionArea.setForeground(AppConfig.TEXT_PRIMARY);
                }
            }
        });
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(null);
        card.add(descScroll, gbc);
        gbc.weighty = 0;
        row++;

        // Status feedback
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        statusLabel = new JLabel(" ");
        statusLabel.setFont(AppConfig.FONT_BODY);
        card.add(statusLabel, gbc);
        row++;

        // Submit button
        gbc.gridy = row;
        JButton submitBtn = new JButton(emoji + "  Submit Report");
        submitBtn.setFont(AppConfig.FONT_BOLD);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(isLost ? AppConfig.DANGER : AppConfig.SUCCESS);
        submitBtn.setBorderPainted(false);
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.setPreferredSize(new Dimension(180, AppConfig.BUTTON_HEIGHT));
        submitBtn.addActionListener(e -> submitReport(submitBtn));
        card.add(submitBtn, gbc);

        // ── Layout ────────────────────────────────────────────────────
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setOpaque(false);
        formWrapper.add(card, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(formWrapper);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppConfig.BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        add(header, BorderLayout.NORTH);
        add(Box.createVerticalStrut(20), BorderLayout.CENTER); // spacer trick for BoxLayout
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(Box.createVerticalStrut(16), BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private void submitReport(JButton btn) {
        String title    = titleField.getText().trim();
        String category = (String) categoryBox.getSelectedItem();
        String color    = (String) colorBox.getSelectedItem();
        String brand    = brandField.getText().trim();
        String location = locationField.getText().trim();
        String desc     = descriptionArea.getText().trim();
        java.util.Date spinDate = (java.util.Date) ((SpinnerDateModel) dateSpinner.getModel()).getValue();
        LocalDate eventDate = spinDate.toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        if ("Select Color".equals(color)) color = null;

        btn.setEnabled(false);
        btn.setText("Saving...");
        final String finalColor = color;

        new SwingWorker<Item, Void>() {
            @Override protected Item doInBackground() {
                return itemService.reportItem(user.getUserId(), title, category, finalColor,
                    brand.isEmpty() ? null : brand, desc, location, eventDate, reportType);
            }
            @Override protected void done() {
                try {
                    Item saved = get();
                    statusLabel.setForeground(AppConfig.SUCCESS);
                    statusLabel.setText("✅  Report submitted successfully! (ID #" + saved.getItemId() + ")");
                    clearForm();
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    statusLabel.setForeground(AppConfig.DANGER);
                    statusLabel.setText("⚠  " + msg);
                } finally {
                    btn.setEnabled(true);
                    btn.setText((reportType == Item.ReportType.LOST ? "📝" : "📦") + "  Submit Report");
                }
            }
        }.execute();
    }

    private void clearForm() {
        titleField.setText("");
        brandField.setText("");
        locationField.setText("");
        categoryBox.setSelectedIndex(0);
        colorBox.setSelectedIndex(0);
        descriptionArea.setText("");
    }

    // ── GBC helper methods ────────────────────────────────────────────────

    private void addLabel(JPanel p, GridBagConstraints gbc, int row, int col, String text) {
        gbc.gridx = col; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConfig.FONT_BOLD);
        lbl.setForeground(AppConfig.TEXT_SECONDARY);
        p.add(lbl, gbc);
    }

    private JTextField addTextField(JPanel p, GridBagConstraints gbc, int row, int col,
                                     String placeholder, int span) {
        gbc.gridx = col; gbc.gridy = row; gbc.gridwidth = span; gbc.weightx = 1.0;
        JTextField f = new JTextField();
        f.setFont(AppConfig.FONT_BODY);
        f.setForeground(AppConfig.TEXT_PRIMARY);
        f.setBackground(AppConfig.BG_INPUT);
        f.setCaretColor(AppConfig.TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConfig.BORDER),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        f.setPreferredSize(new Dimension(0, 36));
        p.add(f, gbc);
        return f;
    }

    private JComboBox<String> addCombo(JPanel p, GridBagConstraints gbc, int row, int col, String[] items) {
        gbc.gridx = col; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.5;
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(AppConfig.FONT_BODY);
        cb.setBackground(AppConfig.BG_INPUT);
        cb.setForeground(AppConfig.TEXT_PRIMARY);
        p.add(cb, gbc);
        return cb;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(AppConfig.FONT_BODY);
        spinner.setBackground(AppConfig.BG_INPUT);
        spinner.setBorder(BorderFactory.createLineBorder(AppConfig.BORDER));
        spinner.setPreferredSize(new Dimension(140, 36));
    }
}
