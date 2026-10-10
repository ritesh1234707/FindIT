package com.findit.service;

import com.findit.dao.UserDAO;
import com.findit.model.User;
import com.findit.util.PasswordUtil;
import com.findit.util.ValidationUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Handles registration, login, and session tracking.
 *
 * Architecture: UI → AuthService (business rules) → UserDAO (database)
 *
 * VIVA TIP: "AuthService sits between the UI and the database.
 * It validates inputs, hashes passwords, and manages the session.
 * This separation means the UI doesn't need to know anything about SQL."
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /** The currently logged-in user. Static so it's accessible app-wide. */
    private static User currentUser;

    /**
     * Registers a new student.
     *
     * @throws IllegalArgumentException for bad input or duplicate email
     * @throws RuntimeException for database errors
     */
    public User register(String fullName, String email,
                          String password, String confirmPassword) {

        String err = ValidationUtil.validateRegistration(fullName, email, password, confirmPassword);
        if (err != null) throw new IllegalArgumentException(err);

        try {
            String normalEmail = email.trim().toLowerCase();
            if (userDAO.emailExists(normalEmail)) {
                throw new IllegalArgumentException("An account with this email already exists.");
            }

            // Hash the password — we NEVER store the plain text
            String hash = PasswordUtil.hash(password);
            User user = new User(fullName.trim(), normalEmail, hash, User.Role.STUDENT);
            int id = userDAO.save(user);
            user.setUserId(id);
            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    /**
     * Logs in a user by verifying credentials.
     *
     * @throws IllegalArgumentException if credentials are wrong
     */
    public User login(String email, String password) {
        if (ValidationUtil.isNullOrBlank(email) || ValidationUtil.isNullOrBlank(password)) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        try {
            Optional<User> opt = userDAO.findByEmail(email.trim().toLowerCase());

            // Generic error — don't reveal whether email or password was wrong
            if (opt.isEmpty() || !PasswordUtil.verify(password, opt.get().getPasswordHash())) {
                throw new IllegalArgumentException("Invalid email or password.");
            }

            currentUser = opt.get();
            return currentUser;

        } catch (SQLException e) {
            throw new RuntimeException("Login error: " + e.getMessage(), e);
        }
    }

    public static void logout()              { currentUser = null; }
    public static User getCurrentUser()      { return currentUser; }
    public static boolean isLoggedIn()       { return currentUser != null; }
}
