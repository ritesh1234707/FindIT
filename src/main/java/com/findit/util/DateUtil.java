package com.findit.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/** Shared date formatting and calculation helpers. */
public class DateUtil {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DISPLAY_DT   = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final DateTimeFormatter INPUT_DATE   = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String format(LocalDate d)    { return d != null ? d.format(DISPLAY_DATE) : "-"; }
    public static String format(LocalDateTime d){ return d != null ? d.format(DISPLAY_DT)   : "-"; }
    public static String toInputFormat(LocalDate d) { return d != null ? d.format(INPUT_DATE) : ""; }

    public static LocalDate parseInput(String s) {
        try { return LocalDate.parse(s, INPUT_DATE); } catch (Exception e) { return null; }
    }

    /** Absolute difference in days between two dates. Used in the matching algorithm. */
    public static long daysBetween(LocalDate a, LocalDate b) {
        if (a == null || b == null) return Long.MAX_VALUE;
        return Math.abs(ChronoUnit.DAYS.between(a, b));
    }

    /** "Just now", "3 hours ago", "2 days ago", etc. */
    public static String timeAgo(LocalDateTime dt) {
        if (dt == null) return "";
        LocalDateTime now = LocalDateTime.now();
        long mins = ChronoUnit.MINUTES.between(dt, now);
        if (mins < 1)  return "Just now";
        if (mins < 60) return mins + " min ago";
        long hrs = ChronoUnit.HOURS.between(dt, now);
        if (hrs < 24)  return hrs + " hr" + (hrs > 1 ? "s" : "") + " ago";
        long days = ChronoUnit.DAYS.between(dt, now);
        if (days < 7)  return days + " day" + (days > 1 ? "s" : "") + " ago";
        return format(dt.toLocalDate());
    }
}
