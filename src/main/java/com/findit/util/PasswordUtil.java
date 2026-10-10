package com.findit.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for BCrypt password hashing.
 *
 * VIVA TIP: "BCrypt is a one-way hash function specifically designed
 * for passwords. It automatically generates a random salt per user, so
 * two users with the same password get different hashes. We use work
 * factor 12, which makes brute-force attacks impractically slow while
 * being fast enough for normal login."
 */
public class PasswordUtil {

    private static final int ROUNDS = 12; // Higher = slower = more secure

    /** Hashes a plaintext password. Store the returned hash, NOT the plain password. */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(ROUNDS));
    }

    /** Returns true if plainPassword matches the stored hash. */
    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (Exception e) {
            return false; // malformed hash
        }
    }

    public static void main(String[] args) {
        System.out.println("STUDENT_HASH=" + hash("Student@123"));
        System.out.println("ADMIN_HASH=" + hash("Admin@123"));
    }
}
