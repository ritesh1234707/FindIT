package com.findit.model;

import java.time.LocalDateTime;

/**
 * Model class representing a FindIT user (Student or Admin).
 *
 * OOP Concepts demonstrated:
 * - Encapsulation: all fields private, accessed via getters/setters
 * - Nested Enum: Role is scoped inside User for clean namespacing
 * - Multiple constructors for different creation scenarios
 */
public class User {

    private int           userId;
    private String        fullName;
    private String        email;
    private String        passwordHash;   // BCrypt hash — NEVER the plain password
    private Role          role;
    private LocalDateTime createdAt;
    private boolean       active;

    /** Enum restricts role values to exactly STUDENT or ADMIN */
    public enum Role {
        STUDENT, ADMIN
    }

    // ── Constructors ──────────────────────────────────────────────────────

    public User() {}

    /** Used when registering a new user (before DB assigns an ID) */
    public User(String fullName, String email, String passwordHash, Role role) {
        this.fullName     = fullName;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.active       = true;
    }

    /** Used when loading an existing user from the database */
    public User(int userId, String fullName, String email, String passwordHash,
                Role role, LocalDateTime createdAt, boolean active) {
        this.userId       = userId;
        this.fullName     = fullName;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.createdAt    = createdAt;
        this.active       = active;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public int           getUserId()      { return userId; }
    public void          setUserId(int u) { this.userId = u; }

    public String        getFullName()         { return fullName; }
    public void          setFullName(String n) { this.fullName = n; }

    public String        getEmail()         { return email; }
    public void          setEmail(String e) { this.email = e; }

    public String        getPasswordHash()         { return passwordHash; }
    public void          setPasswordHash(String h) { this.passwordHash = h; }

    public Role          getRole()         { return role; }
    public void          setRole(Role r)   { this.role = r; }

    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void          setCreatedAt(LocalDateTime t) { this.createdAt = t; }

    public boolean       isActive()          { return active; }
    public void          setActive(boolean a){ this.active = a; }

    // ── Convenience methods ───────────────────────────────────────────────

    public boolean isAdmin()   { return role == Role.ADMIN; }
    public boolean isStudent() { return role == Role.STUDENT; }

    /** Returns first name only, useful for greetings in the UI */
    public String getFirstName() {
        if (fullName == null || fullName.isBlank()) return "";
        return fullName.split(" ")[0];
    }

    @Override
    public String toString() {
        return "User{id=" + userId + ", name='" + fullName + "', role=" + role + "}";
    }
}
