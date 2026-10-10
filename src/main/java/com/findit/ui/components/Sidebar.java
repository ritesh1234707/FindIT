package com.findit.ui.components;

import com.findit.config.AppConfig;
import com.findit.model.User;
import com.findit.service.AuthService;
import com.findit.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Left navigation sidebar shared by all panels in the MainFrame.
 *
 * Navigation items change based on user role (STUDENT vs ADMIN).
 */
public class Sidebar extends JPanel {

    public interface NavListener {
        void onNavigate(String panel);
    }

    private final NavListener listener;
    private JButton activeButton;

    private static final String[][] STUDENT_NAV = {
        {"🏠", "Dashboard",       "DASHBOARD"},
        {"🔍", "Browse Items",    "BROWSE"},
        {"📝", "Report Lost",     "REPORT_LOST"},
        {"📦", "Report Found",    "REPORT_FOUND"},
        {"📋", "My Reports",      "MY_REPORTS"},
        {"✅", "My Claims",       "MY_CLAIMS"},
    };

    private static final String[][] ADMIN_NAV = {
        {"🏠", "Dashboard",       "DASHBOARD"},
        {"🔍", "Browse Items",    "BROWSE"},
        {"📝", "Report Lost",     "REPORT_LOST"},
        {"📦", "Report Found",    "REPORT_FOUND"},
        {"📋", "All Reports",     "MY_REPORTS"},
        {"⚙️",  "Admin Panel",    "ADMIN"},
    };

    public Sidebar(User user, NavListener listener) {
        this.listener = listener;
        setPreferredSize(new Dimension(AppConfig.SIDEBAR_WIDTH, Integer.MAX_VALUE));
        setBackground(AppConfig.BG_SIDEBAR);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppConfig.BORDER));

        // Logo section
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(24, 20, 20, 20));

        JLabel logo = new JLabel("🔍 FindIT");
        logo.setFont(AppConfig.FONT_LOGO);
        logo.setForeground(AppConfig.PRIMARY_LIGHT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Lost & Found Platform");
        tagline.setFont(AppConfig.FONT_SMALL);
        tagline.setForeground(AppConfig.TEXT_MUTED);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(logo);
        logoPanel.add(Box.createVerticalStrut(3));
        logoPanel.add(tagline);

        // Nav items
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        String[][] navItems = user.isAdmin() ? ADMIN_NAV : STUDENT_NAV;
        for (String[] item : navItems) {
            JButton btn = createNavButton(item[0], item[1], item[2]);
            navPanel.add(btn);
            navPanel.add(Box.createVerticalStrut(4));
            if (item[2].equals("DASHBOARD")) {
                setActive(btn);
            }
        }

        // Bottom: user info + logout
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AppConfig.BORDER),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel roleBadge = new JLabel(user.isAdmin() ? "  ADMIN  " : "  STUDENT  ");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleBadge.setForeground(user.isAdmin() ? AppConfig.ACCENT : AppConfig.PRIMARY_LIGHT);
        roleBadge.setOpaque(true);
        roleBadge.setBackground(user.isAdmin()
            ? new Color(255, 165, 0, 30) : new Color(108, 99, 255, 30));
        roleBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleBadge.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        JLabel nameLabel = new JLabel(user.getFullName());
        nameLabel.setFont(AppConfig.FONT_BOLD);
        nameLabel.setForeground(AppConfig.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel emailLabel = new JLabel(user.getEmail());
        emailLabel.setFont(AppConfig.FONT_SMALL);
        emailLabel.setForeground(AppConfig.TEXT_MUTED);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton logoutBtn = new JButton("⏻  Logout");
        logoutBtn.setFont(AppConfig.FONT_SMALL);
        logoutBtn.setForeground(AppConfig.DANGER);
        logoutBtn.setBackground(new Color(255, 71, 87, 20));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> handleLogout());

        bottomPanel.add(roleBadge);
        bottomPanel.add(Box.createVerticalStrut(6));
        bottomPanel.add(nameLabel);
        bottomPanel.add(emailLabel);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(logoutBtn);

        JPanel navWrapper = new JPanel(new BorderLayout());
        navWrapper.setOpaque(false);
        navWrapper.add(navPanel, BorderLayout.NORTH);

        add(logoPanel,   BorderLayout.NORTH);
        add(navWrapper,  BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createNavButton(String icon, String label, String panelKey) {
        JButton btn = new JButton(icon + "  " + label) {
            @Override
            protected void paintComponent(Graphics g) {
                if (this == activeButton) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(108, 99, 255, 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    // Left accent bar
                    g2.setColor(AppConfig.PRIMARY);
                    g2.fillRoundRect(0, 6, 4, getHeight() - 12, 4, 4);
                    g2.dispose();
                } else if (getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(AppConfig.BG_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(AppConfig.FONT_BODY);
        btn.setForeground(AppConfig.TEXT_SECONDARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));

        btn.addActionListener(e -> {
            setActive(btn);
            listener.onNavigate(panelKey);
        });
        return btn;
    }

    private void setActive(JButton btn) {
        if (activeButton != null) {
            activeButton.setForeground(AppConfig.TEXT_SECONDARY);
        }
        activeButton = btn;
        btn.setForeground(AppConfig.PRIMARY_LIGHT);
        repaint();
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.logout();
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) w.dispose();
            SwingUtilities.invokeLater(() -> {
                try {
                    Class<?> loginClass = Class.forName("com.findit.ui.LoginFrame");
                    JFrame login = (JFrame) loginClass.getDeclaredConstructor().newInstance();
                    login.setVisible(true);
                } catch (Exception ex) {
                    System.exit(0);
                }
            });
        }
    }
}
