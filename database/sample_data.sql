-- ============================================================
-- FindIT - Sample Data for Testing
-- These are FICTIONAL records, NOT real student information.
-- Run after schema.sql to populate the database for demo/testing.
-- ============================================================

USE findit_db;

-- Sample students (password for all: Student@123)
INSERT IGNORE INTO users (full_name, email, password_hash, role) VALUES
('Ritesh Kumar',    'ritesh@college.edu',  '$2a$12$u6r/m1Zb/A1OML.DQK77E.JC557xXA8J7kfBOSoTXXJil/7JCOHXW', 'STUDENT'),
('Priya Sharma',   'priya@college.edu',   '$2a$12$u6r/m1Zb/A1OML.DQK77E.JC557xXA8J7kfBOSoTXXJil/7JCOHXW', 'STUDENT'),
('Arjun Singh',    'arjun@college.edu',   '$2a$12$u6r/m1Zb/A1OML.DQK77E.JC557xXA8J7kfBOSoTXXJil/7JCOHXW', 'STUDENT'),
('Anjali Verma',   'anjali@college.edu',  '$2a$12$u6r/m1Zb/A1OML.DQK77E.JC557xXA8J7kfBOSoTXXJil/7JCOHXW', 'STUDENT');

-- Sample LOST reports
INSERT IGNORE INTO items (user_id, title, category, color, brand, description, location, event_date, report_type, status) VALUES
(2, 'College ID Card', 'ID Card', 'White', NULL,
 'My college ID card with photo. Name: Ritesh Kumar, Roll: 21CS101',
 'Near the main canteen', '2026-10-08', 'LOST', 'OPEN'),

(2, 'Black JBL Earbuds', 'Earbuds / Headphones', 'Black', 'JBL',
 'JBL wireless earbuds in a black charging case. Left earbud has a small scratch.',
 'Computer Science lab, 3rd floor', '2026-10-07', 'LOST', 'OPEN'),

(3, 'Blue Backpack', 'Bag / Backpack', 'Blue', 'Wildcraft',
 'Blue Wildcraft backpack with a laptop compartment. Has a keychain attached.',
 'Library reading room', '2026-10-09', 'LOST', 'OPEN'),

(4, 'Scientific Calculator', 'Calculator', 'Black', 'Casio',
 'Casio FX-991EX calculator. Has my name written on the back in marker.',
 'Mathematics classroom, Block B', '2026-10-06', 'LOST', 'OPEN'),

(2, 'Water Bottle', 'Water Bottle', 'Green', 'Milton',
 'Green Milton steel water bottle, 1 litre. Has a dent near the bottom.',
 'Sports ground', '2026-10-08', 'LOST', 'OPEN');

-- Sample FOUND reports
INSERT IGNORE INTO items (user_id, title, category, color, brand, description, location, event_date, report_type, status) VALUES
(3, 'Found: JBL Earbuds', 'Earbuds / Headphones', 'Black', 'JBL',
 'Found a pair of black JBL wireless earbuds with charging case near the CS lab.',
 'Outside CS Lab, 3rd floor', '2026-10-07', 'FOUND', 'OPEN'),

(4, 'Found: Student ID Card', 'ID Card', 'White', NULL,
 'Found a college ID card near the canteen area. Submitted to security desk.',
 'Main canteen entrance', '2026-10-08', 'FOUND', 'OPEN'),

(3, 'Found: Blue Bag', 'Bag / Backpack', 'Blue', NULL,
 'Found a blue backpack in the library. Contains some books and charger.',
 'Library, 1st floor', '2026-10-09', 'FOUND', 'OPEN'),

(4, 'Found: Calculator', 'Calculator', 'Black', 'Casio',
 'Found a Casio calculator in classroom. Has a name written on the back.',
 'Block B, Room 201', '2026-10-06', 'FOUND', 'OPEN');
