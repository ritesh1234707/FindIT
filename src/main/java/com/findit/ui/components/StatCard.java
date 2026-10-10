package com.findit.ui.components;

import com.findit.config.AppConfig;

import javax.swing.*;
import java.awt.*;

/**
 * A reusable dashboard statistics card.
 * Shows a coloured icon, a large number, and a label.
 *
 * Example:   🔴  12
 *            Open Lost Items
 */
public class StatCard extends JPanel {

    private JLabel numberLabel;
    private JLabel titleLabel;

    public StatCard(String title, String iconEmoji, Color accentColor) {
        setLayout(new BorderLayout(0, 8));
        setBackground(AppConfig.BG_CARD);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConfig.BORDER, 1, true),
            BorderFactory.createEmptyBorder(20, 22, 20, 22)
        ));

        // Top row: icon circle + title
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topRow.setOpaque(false);

        // Coloured circle with icon
        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                                      accentColor.getBlue(), 40));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconCircle.setPreferredSize(new Dimension(40, 40));
        iconCircle.setOpaque(false);
        iconCircle.setLayout(new GridBagLayout());

        JLabel iconLabel = new JLabel(iconEmoji);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconCircle.add(iconLabel);

        topRow.add(iconCircle);

        titleLabel = new JLabel(title);
        titleLabel.setFont(AppConfig.FONT_SMALL);
        titleLabel.setForeground(AppConfig.TEXT_SECONDARY);
        topRow.add(titleLabel);

        // Large number
        numberLabel = new JLabel("0");
        numberLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        numberLabel.setForeground(accentColor);

        add(topRow,      BorderLayout.NORTH);
        add(numberLabel, BorderLayout.CENTER);
    }

    /** Updates the displayed number. Can be called from any thread via SwingUtilities.invokeLater. */
    public void setValue(int value) {
        numberLabel.setText(String.valueOf(value));
    }

    public void setValue(String value) {
        numberLabel.setText(value);
    }
}
