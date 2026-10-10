package com.findit.ui.components;

import com.findit.config.AppConfig;

import javax.swing.*;
import java.awt.*;

/**
 * A small coloured badge showing a status string (e.g. "Open", "Returned").
 * Used inside JTable cell renderers and detail panels.
 */
public class StatusBadge extends JLabel {

    public StatusBadge(String text) {
        super(text, JLabel.CENTER);
        setFont(AppConfig.FONT_SMALL);
        setOpaque(false);
        setColor(text);
        setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
    }

    private void setColor(String text) {
        Color fg, bg;
        switch (text.toUpperCase().replace(" ", "_")) {
            case "OPEN"          -> { fg = new Color(46, 213, 115);  bg = new Color(46,  213, 115, 30);  }
            case "RETURNED"      -> { fg = new Color(30, 144, 255);  bg = new Color(30,  144, 255, 30);  }
            case "CLAIM_PENDING",
                 "PENDING",
                 "PENDING_REVIEW"-> { fg = new Color(255, 202, 58);  bg = new Color(255, 202,  58, 30);  }
            case "CLAIMED",
                 "APPROVED"      -> { fg = new Color(108, 99, 255);  bg = new Color(108,  99, 255, 30);  }
            case "REJECTED",
                 "CLOSED"        -> { fg = new Color(255, 71,  87);  bg = new Color(255,  71,  87, 30);  }
            case "LOST"          -> { fg = new Color(255, 71,  87);  bg = new Color(255,  71,  87, 30);  }
            case "FOUND"         -> { fg = new Color(46, 213, 115);  bg = new Color(46,  213, 115, 30);  }
            default              -> { fg = AppConfig.TEXT_SECONDARY; bg = AppConfig.BG_INPUT;            }
        }
        setForeground(fg);
        final Color bgColor = bg;

        // Paint a rounded pill background
        setOpaque(false);
        putClientProperty("bg", bgColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Color bg = (Color) getClientProperty("bg");
        if (bg != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
