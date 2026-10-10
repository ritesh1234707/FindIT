-- ============================================================
-- FindIT - Smart Campus Lost & Found Management System
-- Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS findit_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE findit_db;

-- ─────────────────────────────────────────────
-- USERS TABLE
-- Stores student and admin accounts.
-- Passwords are ALWAYS stored as BCrypt hashes.
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    user_id      INT PRIMARY KEY AUTO_INCREMENT,
    full_name    VARCHAR(100)  NOT NULL,
    email        VARCHAR(150)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role         ENUM('STUDENT', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active    BOOLEAN DEFAULT TRUE,
    INDEX idx_email (email)
);

-- ─────────────────────────────────────────────
-- ITEMS TABLE
-- Stores both LOST and FOUND item reports.
-- report_type distinguishes between them.
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS items (
    item_id      INT PRIMARY KEY AUTO_INCREMENT,
    user_id      INT NOT NULL,
    title        VARCHAR(200)  NOT NULL,
    category     VARCHAR(60)   NOT NULL,
    color        VARCHAR(50),
    brand        VARCHAR(100),
    description  TEXT,
    location     VARCHAR(200)  NOT NULL,
    event_date   DATE          NOT NULL,
    report_type  ENUM('LOST', 'FOUND') NOT NULL,
    status       ENUM('OPEN', 'CLAIM_PENDING', 'CLAIMED', 'RETURNED', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_report_type (report_type),
    INDEX idx_status (status),
    INDEX idx_user_id (user_id),
    FULLTEXT INDEX ft_search (title, description)
);

-- ─────────────────────────────────────────────
-- CLAIMS TABLE
-- Tracks ownership claims submitted by students.
-- Workflow: PENDING -> APPROVED / REJECTED
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS claims (
    claim_id             INT PRIMARY KEY AUTO_INCREMENT,
    item_id              INT NOT NULL,
    claimant_id          INT NOT NULL,
    verification_details TEXT NOT NULL,          -- Private proof of ownership
    status               ENUM('PENDING', 'APPROVED', 'REJECTED', 'WITHDRAWN') NOT NULL DEFAULT 'PENDING',
    reviewed_by          INT,                    -- Admin who reviewed the claim
    review_notes         TEXT,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id)      REFERENCES items(item_id) ON DELETE CASCADE,
    FOREIGN KEY (claimant_id)  REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by)  REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_item_id (item_id),
    INDEX idx_claimant_id (claimant_id),
    INDEX idx_status (status)
);

-- ─────────────────────────────────────────────
-- MATCH SUGGESTIONS TABLE
-- Stores results from the smart matching algorithm.
-- UNIQUE constraint prevents duplicate suggestion pairs.
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS match_suggestions (
    match_id         INT PRIMARY KEY AUTO_INCREMENT,
    lost_item_id     INT NOT NULL,
    found_item_id    INT NOT NULL,
    match_score      INT NOT NULL CHECK (match_score BETWEEN 0 AND 100),
    matching_reasons TEXT,
    status           ENUM('SUGGESTED', 'DISMISSED', 'CONFIRMED') NOT NULL DEFAULT 'SUGGESTED',
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lost_item_id)  REFERENCES items(item_id) ON DELETE CASCADE,
    FOREIGN KEY (found_item_id) REFERENCES items(item_id) ON DELETE CASCADE,
    UNIQUE KEY unique_match_pair (lost_item_id, found_item_id),
    INDEX idx_score (match_score DESC)
);

-- ─────────────────────────────────────────────
-- ADMIN ACCOUNT SETUP
-- Default admin account (password: Admin@123)
-- BCrypt hash of "Admin@123" with 12 rounds.
-- CHANGE THIS PASSWORD in production!
-- ─────────────────────────────────────────────
INSERT IGNORE INTO users (full_name, email, password_hash, role)
VALUES (
    'System Admin',
    'admin@findit.com',
    '$2a$12$FrNlpiM1cjpWk9oYpfICTu5.OD/qL53UaQvpc4P3fgDBsPfn44sqa',
    'ADMIN'
);
-- Note: The hash above is for "Admin@123" - it will be regenerated at runtime if needed
