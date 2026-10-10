package com.findit.util;

import java.util.regex.Pattern;

/**
 * Centralised input validation to avoid duplicating rules in every UI panel.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_RE =
        Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    public static boolean isNullOrBlank(String s) {
        return s == null || s.isBlank();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_RE.matcher(email.trim()).matches();
    }

    /**
     * Validates registration fields.
     * @return error message string, or null if everything is valid
     */
    public static String validateRegistration(String name, String email,
                                               String pass, String confirm) {
        if (isNullOrBlank(name))    return "Full name is required.";
        if (name.trim().length() < 2) return "Name must be at least 2 characters.";
        if (isNullOrBlank(email))   return "Email address is required.";
        if (!isValidEmail(email))   return "Please enter a valid email (e.g. you@college.edu).";
        if (isNullOrBlank(pass))    return "Password is required.";
        if (pass.length() < 6)     return "Password must be at least 6 characters.";
        if (!pass.equals(confirm))  return "Passwords do not match.";
        return null;
    }

    /**
     * Validates an item report form.
     * @return error message string, or null if valid
     */
    public static String validateItemReport(String title, String category,
                                             String location, String date) {
        if (isNullOrBlank(title))                    return "Item title is required.";
        if (title.trim().length() < 3)               return "Title must be at least 3 characters.";
        if (isNullOrBlank(category)
            || category.startsWith("Select"))        return "Please select a category.";
        if (isNullOrBlank(location))                 return "Location is required.";
        if (isNullOrBlank(date))                     return "Please select the date.";
        return null;
    }
}
