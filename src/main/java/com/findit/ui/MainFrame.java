package com.findit.ui;

import com.findit.config.AppConfig;
import com.findit.model.Item;
import com.findit.model.User;
import com.findit.ui.components.Sidebar;
import com.findit.ui.panels.*;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window shown after successful login.
 *
 * Layout: Sidebar (left) | Content Panel (right)
 * Navigation is handled by the Sidebar component.
 */
public class MainFrame extends JFrame implements Sidebar.NavListener {

    private final User   user;
    private JPanel       contentArea;
    private CardLayout   cardLayout;

    // Panel identifiers (must match Sidebar navigation keys)
    private static final String DASHBOARD    = "DASHBOARD";
    private static final String BROWSE       = "BROWSE";
    private static final String REPORT_LOST  = "REPORT_LOST";
    private static final String REPORT_FOUND = "REPORT_FOUND";
    private static final String MY_REPORTS   = "MY_REPORTS";
    private static final String MY_CLAIMS    = "MY_CLAIMS";
    private static final String ADMIN        = "ADMIN";

    public MainFrame(User user) {
        this.user = user;
        setTitle("FindIT — " + (user.isAdmin() ? "Admin" : user.getFullName()));
        setSize(AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppConfig.BG_DARK);

        // Sidebar on the left
        Sidebar sidebar = new Sidebar(user, this);

        // CardLayout panel on the right — swaps panels on nav click
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(AppConfig.BG_DARK);

        // Register all panels
        contentArea.add(new DashboardPanel(user),                          DASHBOARD);
        contentArea.add(new BrowseItemsPanel(user),                        BROWSE);
        contentArea.add(new ReportItemPanel(user, Item.ReportType.LOST),   REPORT_LOST);
        contentArea.add(new ReportItemPanel(user, Item.ReportType.FOUND),  REPORT_FOUND);
        contentArea.add(new MyReportsPanel(user),                          MY_REPORTS);
        contentArea.add(new MyClaimsPanel(user),                           MY_CLAIMS);

        if (user.isAdmin()) {
            contentArea.add(new AdminPanel(user), ADMIN);
        }

        cardLayout.show(contentArea, DASHBOARD);

        root.add(sidebar,     BorderLayout.WEST);
        root.add(contentArea, BorderLayout.CENTER);
        setContentPane(root);
    }

    /** Called by Sidebar when a nav button is clicked. */
    @Override
    public void onNavigate(String panel) {
        cardLayout.show(contentArea, panel);
    }
}
