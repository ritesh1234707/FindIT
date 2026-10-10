package com.findit.config;

import java.awt.*;

/**
 * Central configuration for FindIT's colors, fonts, and UI constants.
 *
 * VIVA TIP: "We used a central AppConfig class to define the design system.
 * This is the DRY principle (Don't Repeat Yourself) — one place to change
 * colors and fonts for the entire application."
 */
public class AppConfig {

    // ── App Identity ──────────────────────────────────────────────────────
    public static final String APP_NAME    = "FindIT";
    public static final String APP_TAGLINE = "Smart Campus Lost & Found";
    public static final String APP_VERSION = "1.0.0";

    // ── Color Palette ────────────────────────────────────────────────────
    // Primary brand colors
    public static final Color PRIMARY       = new Color(108, 99, 255);   // Indigo/Purple
    public static final Color PRIMARY_DARK  = new Color(82, 73, 210);    // Darker variant
    public static final Color PRIMARY_LIGHT = new Color(140, 132, 255);  // Lighter variant
    public static final Color ACCENT        = new Color(255, 165, 0);    // Amber

    // Semantic colors
    public static final Color SUCCESS = new Color(46, 213, 115);   // Green
    public static final Color WARNING = new Color(255, 202, 58);   // Yellow
    public static final Color DANGER  = new Color(255, 71, 87);    // Red
    public static final Color INFO    = new Color(30, 144, 255);   // Blue

    // Background hierarchy
    public static final Color BG_DARK    = new Color(14, 14, 24);    // Main window background
    public static final Color BG_SIDEBAR = new Color(18, 18, 32);    // Left sidebar
    public static final Color BG_CARD    = new Color(26, 26, 46);    // Cards and panels
    public static final Color BG_INPUT   = new Color(32, 32, 54);    // Input fields
    public static final Color BG_HOVER   = new Color(40, 40, 65);    // Hover state

    // Text colors
    public static final Color TEXT_PRIMARY   = new Color(235, 235, 245);  // Main text
    public static final Color TEXT_SECONDARY = new Color(155, 155, 185);  // Subtitles, labels
    public static final Color TEXT_MUTED     = new Color(95, 95, 125);    // Placeholders

    // Border color
    public static final Color BORDER       = new Color(45, 45, 72);
    public static final Color BORDER_FOCUS = new Color(108, 99, 255);

    // ── Typography ───────────────────────────────────────────────────────
    public static final Font FONT_LOGO       = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING    = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_SUBHEADING = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD       = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_LARGE      = new Font("Segoe UI", Font.PLAIN, 15);

    // ── Dimensions ───────────────────────────────────────────────────────
    public static final int SIDEBAR_WIDTH  = 225;
    public static final int TOPBAR_HEIGHT  = 60;
    public static final int WINDOW_WIDTH   = 1200;
    public static final int WINDOW_HEIGHT  = 720;
    public static final int CARD_ARC       = 14;
    public static final int BUTTON_HEIGHT  = 40;

    // ── Dropdown Options ─────────────────────────────────────────────────
    public static final String[] CATEGORIES = {
        "Select Category", "ID Card", "Wallet / Purse", "Keys",
        "Phone / Tablet", "Laptop / Charger", "Earbuds / Headphones",
        "Books / Notes", "Water Bottle", "Watch / Jewelry",
        "Bag / Backpack", "Clothing", "Calculator", "Other"
    };

    public static final String[] COLORS = {
        "Select Color", "Black", "White", "Grey", "Silver",
        "Red", "Blue", "Green", "Yellow", "Orange",
        "Purple", "Pink", "Brown", "Gold", "Multicolor", "Other"
    };
}
