package com.findit;

import com.findit.api.ApiServer;
import com.findit.config.DatabaseConfig;
import com.findit.service.MatchingService;
import com.findit.ui.LoginFrame;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.Desktop;
import java.net.URI;

/**
 * FindIT — Smart Campus Lost & Found System
 *
 * Entry point of the application.
 * Starts the Java REST API Server on port 8080 and opens the Web Application.
 * Optional flag: --gui to launch the legacy Swing desktop window.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("---------------------------------------------");
        System.out.println("  Starting FindIT - Smart Campus Lost & Found");
        System.out.println("---------------------------------------------");

        // ── Step 1: Connect to MySQL ──────────────────────────────────
        try {
            DatabaseConfig.initialize();
            System.out.println("✓ MySQL Database connection established successfully.");
        } catch (Exception e) {
            System.err.println("✗ Database Connection Failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // ── Step 2: Run heuristic matching pass in background ─────────
        new Thread(() -> {
            try {
                MatchingService matchingService = new MatchingService();
                matchingService.runFullPass();
                System.out.println("✓ Initial smart matching scan completed.");
            } catch (Exception e) {
                System.err.println("! Match scan skipped: " + e.getMessage());
            }
        }).start();

        // ── Step 3: Check if GUI mode requested ───────────────────────
        boolean guiMode = false;
        for (String arg : args) {
            if ("--gui".equalsIgnoreCase(arg) || "--desktop".equalsIgnoreCase(arg)) {
                guiMode = true;
                break;
            }
        }

        if (guiMode) {
            FlatDarkLaf.setup();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });
            return;
        }

        // ── Step 4: Start Java Web & REST API Server ──────────────────
        try {
            ApiServer apiServer = new ApiServer();
            apiServer.start();

            // Open web app automatically in user's browser
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                new Thread(() -> {
                    try {
                        Thread.sleep(800);
                        Desktop.getDesktop().browse(new URI("http://localhost:8080"));
                    } catch (Exception ignored) {}
                }).start();
            }
        } catch (Exception e) {
            System.err.println("✗ Failed to start HTTP server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
