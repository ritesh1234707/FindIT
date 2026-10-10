package com.findit.ui;

import com.findit.config.AppConfig;
import com.findit.model.User;
import com.findit.service.AuthService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Login screen for FindIT.
 *
 * Swing concepts demonstrated:
 * - JFrame layout using GridLayout (split view)
 * - BoxLayout for vertical form stacking
 * - Custom paintComponent() for gradient background
 * - FocusListener for placeholder text
 * - SwingWorker for non-blocking login
 */
public class LoginFrame extends JFrame {

    private JTextField    emailField;
    private JPasswordField passwordField;
    private JLabel        statusLabel;
    private JButton       loginBtn;
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("FindIT — Login");
        setSize(920, 580);
        setMinimumSize(new Dimension(800, 520));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(AppConfig.BG_DARK);
        root.add(buildBrandPanel());
        root.add(buildFormPanel());
        setContentPane(root);
    }

    // ── Left: Brand panel ──────────────────────────────────────────────────

    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(75, 50, 200),
                                               getWidth(), getHeight(), new Color(20, 20, 60)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Decorative circles
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(getWidth() - 160, -90, 260, 260);
                g2.fillOval(-70, getHeight() - 210, 270, 270);
                g2.dispose();
            }
        };
        panel.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL;

        JLabel iconLbl = new JLabel("🔍", JLabel.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        g.gridy = 0; g.insets = new Insets(0, 40, 6, 40);
        panel.add(iconLbl, g);

        JLabel nameLbl = new JLabel("FindIT", JLabel.CENTER);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 38));
        nameLbl.setForeground(Color.WHITE);
        g.gridy = 1; g.insets = new Insets(0, 40, 4, 40);
        panel.add(nameLbl, g);

        JLabel tagLbl = new JLabel(AppConfig.APP_TAGLINE, JLabel.CENTER);
        tagLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tagLbl.setForeground(new Color(255, 255, 255, 180));
        g.gridy = 2; g.insets = new Insets(0, 40, 32, 40);
        panel.add(tagLbl, g);

        for (String f : new String[]{"📱  Report lost items instantly",
                                      "🔍  Smart matching algorithm",
                                      "✅  Verified ownership claims"}) {
            JLabel lbl = new JLabel(f);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setForeground(new Color(255, 255, 255, 200));
            g.gridy++;
            g.insets = new Insets(5, 60, 5, 40);
            panel.add(lbl, g);
        }
        return panel;
    }

    // ── Right: Form panel ──────────────────────────────────────────────────

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(AppConfig.BG_DARK);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppConfig.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(AppConfig.BORDER, 1, true),
            new EmptyBorder(42, 42, 42, 42)
        ));

        addLeft(card, label("Welcome Back", AppConfig.FONT_TITLE, AppConfig.TEXT_PRIMARY));
        addLeft(card, label("Sign in to your FindIT account", AppConfig.FONT_BODY, AppConfig.TEXT_SECONDARY));
        card.add(Box.createVerticalStrut(28));

        addLeft(card, label("Email Address", AppConfig.FONT_BOLD, AppConfig.TEXT_SECONDARY));
        card.add(Box.createVerticalStrut(6));
        emailField = textField("Enter your email");
        addLeft(card, emailField);
        card.add(Box.createVerticalStrut(16));

        addLeft(card, label("Password", AppConfig.FONT_BOLD, AppConfig.TEXT_SECONDARY));
        card.add(Box.createVerticalStrut(6));
        passwordField = passwordField("Enter your password");
        addLeft(card, passwordField);
        card.add(Box.createVerticalStrut(20));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(AppConfig.FONT_SMALL);
        statusLabel.setForeground(AppConfig.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(8));

        loginBtn = primaryButton("Sign In");
        loginBtn.addActionListener(e -> doLogin());
        addLeft(card, loginBtn);
        card.add(Box.createVerticalStrut(16));

        // Register link
        JPanel regRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        regRow.setOpaque(false);
        regRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        regRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel txt = new JLabel("Don't have an account?");
        txt.setFont(AppConfig.FONT_BODY);
        txt.setForeground(AppConfig.TEXT_SECONDARY);
        JButton regLink = new JButton("Register here");
        regLink.setFont(AppConfig.FONT_BODY);
        regLink.setForeground(AppConfig.PRIMARY_LIGHT);
        regLink.setBorderPainted(false);
        regLink.setContentAreaFilled(false);
        regLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        regLink.addActionListener(e -> new RegisterFrame(this).setVisible(true));
        regRow.add(txt); regRow.add(regLink);
        card.add(regRow);

        // Enter key support
        emailField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(e -> doLogin());

        outer.add(card, new GridBagConstraints());
        return outer;
    }

    // ── Login logic ───────────────────────────────────────────────────────

    private void doLogin() {
        String email = emailField.getText().trim();
        String pass  = new String(passwordField.getPassword());

        if (email.equals("Enter your email"))    email = "";
        if (pass.equals("Enter your password"))  pass  = "";

        final String fe = email, fp = pass;
        loginBtn.setEnabled(false);
        loginBtn.setText("Signing in...");
        statusLabel.setText(" ");

        new SwingWorker<User, Void>() {
            @Override protected User doInBackground() { return authService.login(fe, fp); }
            @Override protected void done() {
                try {
                    User user = get();
                    dispose();
                    SwingUtilities.invokeLater(() -> new MainFrame(user).setVisible(true));
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    statusLabel.setText("⚠  " + msg);
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Sign In");
                }
            }
        }.execute();
    }

    // ── Style helpers ─────────────────────────────────────────────────────

    private JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void addLeft(JPanel p, Component c) {
        if (c instanceof JComponent jc) jc.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(c);
    }

    private JTextField textField(String placeholder) {
        JTextField f = new JTextField(placeholder);
        f.setFont(AppConfig.FONT_BODY);
        f.setForeground(AppConfig.TEXT_MUTED);
        f.setBackground(AppConfig.BG_INPUT);
        f.setCaretColor(AppConfig.TEXT_PRIMARY);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setBorder(new CompoundBorder(new LineBorder(AppConfig.BORDER, 1, true),
                                        new EmptyBorder(10, 14, 10, 14)));
        f.addFocusListener(placeholderFocus(f, placeholder));
        return f;
    }

    private JPasswordField passwordField(String placeholder) {
        JPasswordField f = new JPasswordField(placeholder);
        f.setFont(AppConfig.FONT_BODY);
        f.setForeground(AppConfig.TEXT_MUTED);
        f.setBackground(AppConfig.BG_INPUT);
        f.setCaretColor(AppConfig.TEXT_PRIMARY);
        f.setEchoChar((char) 0);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setBorder(new CompoundBorder(new LineBorder(AppConfig.BORDER, 1, true),
                                        new EmptyBorder(10, 14, 10, 14)));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (new String(f.getPassword()).equals(placeholder)) {
                    f.setText(""); f.setForeground(AppConfig.TEXT_PRIMARY); f.setEchoChar('•');
                }
                f.setBorder(new CompoundBorder(new LineBorder(AppConfig.BORDER_FOCUS, 1, true),
                                                new EmptyBorder(10, 14, 10, 14)));
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getPassword().length == 0) {
                    f.setText(placeholder); f.setForeground(AppConfig.TEXT_MUTED); f.setEchoChar((char) 0);
                }
                f.setBorder(new CompoundBorder(new LineBorder(AppConfig.BORDER, 1, true),
                                                new EmptyBorder(10, 14, 10, 14)));
            }
        });
        return f;
    }

    private FocusAdapter placeholderFocus(JTextField f, String placeholder) {
        return new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(AppConfig.TEXT_PRIMARY); }
                f.setBorder(new CompoundBorder(new LineBorder(AppConfig.BORDER_FOCUS, 1, true),
                                                new EmptyBorder(10, 14, 10, 14)));
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().isBlank()) { f.setText(placeholder); f.setForeground(AppConfig.TEXT_MUTED); }
                f.setBorder(new CompoundBorder(new LineBorder(AppConfig.BORDER, 1, true),
                                                new EmptyBorder(10, 14, 10, 14)));
            }
        };
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? AppConfig.PRIMARY_DARK
                             : getModel().isRollover() ? AppConfig.PRIMARY_LIGHT
                             : AppConfig.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        b.setFont(AppConfig.FONT_BOLD);
        b.setForeground(Color.WHITE);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConfig.BUTTON_HEIGHT));
        b.setPreferredSize(new Dimension(300, AppConfig.BUTTON_HEIGHT));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}
