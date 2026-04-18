-- =======================================================
-- V6: Finance Requirement Groups + Tracks + Math Minor
-- Run AFTER V5__finance_degree_plan.sql
-- group_id continues from 11, course_id continues from 141
-- =======================================================

SET FOREIGN_KEY_CHECKS = 0;

-- =======================================================
-- 1. STUDENT COLUMNS
-- =======================================================
-- Track choice for Finance majors. Allowed: STANDARD, RE, PFP, CBC, GEM, ECTC
ALTER TABLE student ADD COLUMN finance_track VARCHAR(20) DEFAULT 'STANDARD';

-- Optional Math Minor add-on (used by Finance majors who add math minor on top of their track)
ALTER TABLE student ADD COLUMN math_minor BOOLEAN NOT NULL DEFAULT FALSE;


-- =======================================================
-- 2. EXTRA COURSES — Finance Math Minor placeholders
-- (Reuses Calc I (15) + Calc II (16) + MINOR-3XXX (76) + MINOR-4XXX (77),
--  but Finance students need 2 more 3000+/4000+ math electives.)
-- =======================================================
INSERT INTO course (course_id, subject, number, title, credits) VALUES
(142, 'MATH', 'FIN-MINOR-3XXX', 'Math Minor Elective (3000+ Level)', 3),
(143, 'MATH', 'FIN-MINOR-4XXX', 'Math Minor Elective (4000+ Level)', 3);


-- =======================================================
-- 3. REQUIREMENT GROUPS  (program_id = 2)
-- =======================================================

INSERT INTO requirement_group (group_id, program_id, name, rule_type, min_credits, min_courses, parent_group_id)
VALUES
(12, 2, 'Finance Foundation',                                   'ALL_OF', NULL, NULL, NULL),
(13, 2, 'Finance Core',                                         'ALL_OF', NULL, NULL, NULL),
(14, 2, 'Finance Track: Standard',                              'ALL_OF', NULL, NULL, NULL),
(15, 2, 'Finance Track: Real Estate',                           'ALL_OF', NULL, NULL, NULL),
(16, 2, 'Finance Track: Personal Financial Planning',           'ALL_OF', NULL, NULL, NULL),
(17, 2, 'Finance Track: Corporate Banking and Credit',          'ALL_OF', NULL, NULL, NULL),
(18, 2, 'Finance Track: Global Energy Management',              'ALL_OF', NULL, NULL, NULL),
(19, 2, 'Finance Track: Energy Commodities Trading and Consulting', 'ALL_OF', NULL, NULL, NULL),
(20, 2, 'Finance Math Minor',                                   'ALL_OF', NULL, NULL, NULL),
(21, 2, 'Finance Communication',                                'ALL_OF', NULL, NULL, NULL),
(22, 2, 'Finance History and Gov',                              'ALL_OF', NULL, NULL, NULL),
(23, 2, 'Finance Additional Core',                              'ALL_OF', NULL, NULL, NULL),
(24, 2, 'Finance Natural Science',                              'ALL_OF', NULL, NULL, NULL);


-- =======================================================
-- 4. REQUIREMENT ITEMS
-- =======================================================

-- ---- (12) Finance Foundation -------------------------
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(12, 87,  NULL, TRUE, 'C'),  -- MATH 1314 College Algebra
(12, 88,  NULL, TRUE, 'C'),  -- MATH 1324 Math for Business and Social Sciences I
(12, 81,  NULL, TRUE, 'C'),  -- ACCT 2301 Principles of Financial Accounting
(12, 82,  NULL, TRUE, 'C'),  -- ACCT 2302 Principles of Managerial Accounting
(12, 85,  NULL, TRUE, 'C'),  -- ECON 2301 Principles of Macroeconomics
(12, 86,  NULL, TRUE, 'C'),  -- ECON 2302 Principles of Microeconomics
(12, 84,  NULL, TRUE, 'C'),  -- STAT 3331 Statistical Analysis for Business Apps I
(12, 83,  NULL, TRUE, 'C');  -- FINA 3332 Principles of Financial Management

-- ---- (13) Finance Core (required for ALL Finance majors) ----
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(13, 89,  NULL, TRUE, 'C'),  -- FINA 4320 Investment Management
(13, 90,  NULL, TRUE, 'C');  -- FINA 4330 Corporate Finance

-- ---- (14) Standard Track: 12 cr of upper-div finance electives ----
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(14, 138, NULL, TRUE, 'C'),  -- STD-ELEC-1
(14, 139, NULL, TRUE, 'C'),  -- STD-ELEC-2
(14, 140, NULL, TRUE, 'C'),  -- STD-ELEC-3
(14, 141, NULL, TRUE, 'C');  -- STD-ELEC-4

-- ---- (15) Real Estate Track ----
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(15, 94,  NULL, TRUE, 'C'),  -- FINA 4380 Real Estate Finance and Investment
(15, 95,  NULL, TRUE, 'C'),  -- FINA 4383 RE Market Research and Valuation
(15, 96,  NULL, TRUE, 'C'),  -- FINA 4382 Developing a Real Estate Project
(15, 97,  NULL, TRUE, 'C');  -- FINA 4397 Asset Management

-- ---- (16) Personal Financial Planning Track ----
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(16, 98,  NULL, TRUE, 'C'),  -- FINA 4352 Financial Planning for Professionals
(16, 99,  NULL, TRUE, 'C'),  -- FINA 4325 Retirement and Estate Planning
(16, 100, NULL, TRUE, 'C'),  -- FINA 4353 Practicum in Personal Financial Planning
(16, 101, NULL, TRUE, 'C'),  -- FINA 4354 Risk Management
(16, 102, NULL, TRUE, 'C');  -- ACCT 4331 Federal Income Tax

-- ---- (17) Corporate Banking and Credit Track ----
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(17, 91,  NULL, TRUE, 'C'),  -- ACCT 3366 Financial Reporting Frameworks
(17, 92,  NULL, TRUE, 'C'),  -- ACCT 3367 Intermediate Accounting I
(17, 93,  NULL, TRUE, 'C'),  -- ACCT 3368 Intermediate Accounting II
(17, 103, NULL, TRUE, 'C'),  -- FINA 4341 Commercial Bank Management
(17, 104, NULL, TRUE, 'C'),  -- FINA 4342 Financial Eval of Corporate Reports
(17, 105, NULL, TRUE, 'C');  -- FINA 4343 Credit Analysis

-- ---- (18) Global Energy Management Track ----
-- Required core: FINA 4360 (Intl Finance), 4370 (Energy Trading), 4371 (Energy Value Chain)
-- Plus 12 cr of GEM electives + 6 cr of GEM advanced electives
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(18, 111, NULL, TRUE, 'C'),  -- FINA 4360 International Financial Management
(18, 112, NULL, TRUE, 'C'),  -- FINA 4370 Energy Trading
(18, 113, NULL, TRUE, 'C'),  -- FINA 4371 Energy Value Chain
(18, 127, NULL, TRUE, 'C'),  -- GEM-ELEC-1
(18, 128, NULL, TRUE, 'C'),  -- GEM-ELEC-2
(18, 129, NULL, TRUE, 'C'),  -- GEM-ELEC-3
(18, 130, NULL, TRUE, 'C'),  -- GEM-ELEC-4
(18, 131, NULL, TRUE, 'C'),  -- GEM-ADV-1
(18, 132, NULL, TRUE, 'C');  -- GEM-ADV-2

-- ---- (19) Energy Commodities Trading and Consulting Track ----
-- Required core: FINA 4396 (Internship/Experiential Learning)
-- Plus 5 ECT&C electives
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(19, 126, NULL, TRUE, 'C'),  -- FINA 4396 Finance Internship / Experiential Learning
(19, 133, NULL, TRUE, 'C'),  -- ECTC-ELEC-1
(19, 134, NULL, TRUE, 'C'),  -- ECTC-ELEC-2
(19, 135, NULL, TRUE, 'C'),  -- ECTC-ELEC-3
(19, 136, NULL, TRUE, 'C'),  -- ECTC-ELEC-4
(19, 137, NULL, TRUE, 'C');  -- ECTC-ELEC-5

-- ---- (20) Finance Math Minor (optional add-on) ----
-- 18 credits beyond the foundation math: Calc I + Calc II + 4 math electives 3000+/4000+
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(20, 15,  NULL, TRUE, 'C'),  -- MATH 2413 Calculus I
(20, 16,  NULL, TRUE, 'C'),  -- MATH 2414 Calculus II
(20, 76,  NULL, TRUE, 'C'),  -- MATH MINOR-3XXX (existing placeholder)
(20, 77,  NULL, TRUE, 'C'),  -- MATH MINOR-4XXX (existing placeholder)
(20, 142, NULL, TRUE, 'C'),  -- MATH FIN-MINOR-3XXX
(20, 143, NULL, TRUE, 'C');  -- MATH FIN-MINOR-4XXX

-- ---- (21) Finance Communication ----
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(21, 22, NULL, TRUE, 'C'),  -- ENGL 1301
(21, 23, NULL, TRUE, 'C');  -- ENGL 1302

-- ---- (22) Finance History and Gov ----
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(22, 24, NULL, TRUE, 'C'),  -- HIST 1301
(22, 25, NULL, TRUE, 'C'),  -- HIST 1302
(22, 26, NULL, TRUE, 'C'),  -- GOVT 2305
(22, 27, NULL, TRUE, 'C');  -- GOVT 2306

-- ---- (23) Finance Additional Core ----
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(23, 28, NULL, TRUE, NULL),  -- LPC
(23, 29, NULL, TRUE, NULL),  -- CA
(23, 30, NULL, TRUE, NULL),  -- SBS
(23, 31, NULL, TRUE, NULL);  -- WID

-- ---- (24) Finance Natural Science (lecture only — Bauer doesn't require labs) ----
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(24, 34, NULL, TRUE, NULL),  -- NSM LEC-3HR-1
(24, 35, NULL, TRUE, NULL);  -- NSM LEC-3HR-2


-- =======================================================
-- 5. PREREQ RULES for Finance Math Minor placeholders
-- node_id continues from 152, rule_id continues from 65
-- =======================================================

-- FIN-MINOR-3XXX (course 142) requires MATH 2414 (course 16)
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(153, 'COURSE', 16, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES
(66, 142, 'PREREQ', 153);
UPDATE requisite_node SET rule_id = 66 WHERE node_id = 153;

-- FIN-MINOR-4XXX (course 143) requires FIN-MINOR-3XXX (course 142)
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(154, 'COURSE', 142, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES
(67, 143, 'PREREQ', 154);
UPDATE requisite_node SET rule_id = 67 WHERE node_id = 154;

SET FOREIGN_KEY_CHECKS = 1;
