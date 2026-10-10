package com.findit.ui;

import com.findit.config.AppConfig;
import com.findit.model.User;
import com.findit.service.AuthService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Student registration dialog.
 * Opens on top of the LoginFrame and disposes itself on success.
 */
public class RegisterFrame extends JDialog {

    private JTextField     nameField, emailField;
    private JPasswordField passField, confirmField;
    private JLabel         statusLabel;
    private JButton        registerBtn;
    private final AuthService authService = new AuthService();

    public RegisterFrame(JFrame parent) {
        super(parent, "FindIT — Create Account", true);
        setSize(520, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel();
        root.setBackground(AppConfig.BG_DARK);
        root.setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppConfig.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(AppConfig.BORDER, 1, true),
            new EmptyBorder(36, 36, 36, 36)
        ));

        JLabel title = new JLabel("Create Your Account");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Join FindIT and help your campus community.");
        sub.setFont(AppConfig.FONT_BODY);
        sub.setForeground(AppConfig.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));

        nameField    = addField(card, "Full Name",        "Your full name");
        emailField   = addField(card, "Email Address",    "your@college.edu");
        passField    = addPasswordField(card, "Password", "At least 6 characters");
        confirmField = addPasswordField(card, "Confirm Password", "Re-enter password");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(AppConfig.FONT_SMALL);
        statusLabel.setForeground(AppConfig.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(8));

        registerBtn = new JButton("Create Account");
        registerBtn.setFont(AppConfig.FONT_BOLD);
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBackground(AppConfig.PRIMARY);
        registerBtn.setBorderPainted(false);
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConfig.BUTTON_HEIGHT));
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.addActionListener(e -> doRegister());
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(14));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(AppConfig.FONT_BODY);
        cancelBtn.setForeground(AppConfig.TEXT_SECONDARY);
        cancelBtn.setBackground(AppConfig.BG_INPUT);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConfig.BUTTON_HEIGHT));
        cancelBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancelBtn.addActionListener(e -> dispose());
        card.add(cancelBtn);

        root.add(card, new GridBagConstraints());
        setContentPane(root);
    }

    private JTextField addField(JPanel p, String labelText, String placeholder) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(AppConfig.FONT_BOLD);
        lbl.setForeground(AppConfig.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(5));

        JTextField f = new JTextField(placeholder);
        f.setFont(AppConfig.FONT_BODY);
        f.setForeground(AppConfig.TEXT_MUTED);
        f.setBackground(AppConfig.BG_INPUT);
        f.setCaretColor(AppConfig.TEXT_PRIMARY);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setBorder(new CompoundBorder(
            new LineBorder(AppConfig.BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(AppConfig.TEXT_PRIMARY); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().isBlank()) { f.setText(placeholder); f.setForeground(AppConfig.TEXT_MUTED); }
            }
        });

        p.add(f);
        p.add(Box.createVerticalStrut(12));
        return f;
    }

    private JPasswordField addPasswordField(JPanel p, String labelText, String placeholder) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(AppConfig.FONT_BOLD);
        lbl.setForeground(AppConfig.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(5));

        JPasswordField f = new JPasswordField(placeholder);
        f.setFont(AppConfig.FONT_BODY);
        f.setForeground(AppConfig.TEXT_MUTED);
        f.setBackground(AppConfig.BG_INPUT);
        f.setCaretColor(AppConfig.TEXT_PRIMARY);
        f.setEchoChar((char) 0);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setBorder(new CompoundBorder(
            new LineBorder(AppConfig.BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (new String(f.getPassword()).equals(placeholder)) {
                    f.setText(""); f.setForeground(AppConfig.TEXT_PRIMARY); f.setEchoChar('•');
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getPassword().length == 0) {
                    f.setText(placeholder); f.setForeground(AppConfig.TEXT_MUTED); f.setEchoChar((char) 0);
                }
            }
        });

        p.add(f);
        p.add(Box.createVerticalStrut(12));
        return f;
    }

    private void doRegister() {
        String name    = nameField.getText().trim();
        String email   = emailField.getText().trim();
        String pass    = new String(passField.getPassword());
        String confirm = new String(confirmField.getPassword());

        // Clean up placeholder text
        if (name.equals("Your full name"))         name    = "";
        if (email.equals("your@college.edu"))       email   = "";
        if (pass.equals("At least 6 characters"))   pass    = "";
        if (confirm.equals("Re-enter password"))    confirm = "";

        final String fn = name, fe = email, fp = pass, fc = confirm;

        registerBtn.setEnabled(false);
        registerBtn.setText("Creating account...");
        statusLabel.setText(" ");

        new SwingWorker<User, Void>() {
            @Override protected User doInBackground() {
                return authService.register(fn, fe, fp, fc);
            }
            @Override protected void done() {
                try {
                    User user = get();
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                        "✅  Account created successfully!\nYou can now log in.",
                        "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    statusLabel.setText("⚠  " + msg);
                    registerBtn.setEnabled(true);
                    registerBtn.setText("Create Account");
                }
            }
        }.execute();
    }
}
