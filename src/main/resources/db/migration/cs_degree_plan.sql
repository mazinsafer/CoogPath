-- =======================================================
-- CoogPath Seed Data v3
-- Run AFTER coogpath.sql
-- =======================================================

USE coogpath;

SET FOREIGN_KEY_CHECKS = 0;

-- =======================================================
-- 1. COURSES
-- =======================================================

INSERT INTO course (course_id, subject, number, title, credits) VALUES
-- CS Core
(1,  'COSC', '1336',        'Computer Science and Programming',           3),
(2,  'COSC', '1437',        'Introduction to Programming',                4),
(3,  'COSC', '2436',        'Programming and Data Structures',            4),
(4,  'COSC', '2425',        'Computer Organization and Architecture',     4),
(5,  'COSC', '3320',        'Algorithms and Data Structures',             3),
(6,  'COSC', '3340',        'Introduction to Automata and Computability', 3),
(7,  'COSC', '3360',        'Fundamentals of Operating Systems',          3),
(8,  'COSC', '3380',        'Database Systems',                           3),
(9,  'COSC', '4351',        'Fundamentals of Software Engineering',       3),
(38, 'COSC', '4353',        'Software Design',                            3),
-- CS Advanced Elective placeholders
(10, 'COSC', '3XXX-ELEC-1', 'CS Advanced Elective 1',                    3),
(11, 'COSC', '3XXX-ELEC-2', 'CS Advanced Elective 2',                    3),
(12, 'COSC', '3XXX-ELEC-3', 'CS Advanced Elective 3',                    3),
(13, 'COSC', '3XXX-ELEC-4', 'CS Advanced Elective 4',                    3),
(14, 'COSC', '3XXX-ELEC-5', 'CS Advanced Elective 5',                    3),
-- Math
(15, 'MATH', '2413',        'Calculus I',                                 4),
(16, 'MATH', '2414',        'Calculus II',                                4),
(17, 'MATH', '2305',        'Discrete Mathematics',                       3),
(18, 'COSC', '2305',        'Computing Structures',                       3),
(19, 'MATH', '2318',        'Linear Algebra',                             3),
(20, 'MATH', '3321',        'Engineering Mathematics',                    3),
(21, 'MATH', '3339',        'Statistics for the Sciences',                3),
-- Core Communication
(22, 'ENGL', '1301',        'First Year Writing I',                       3),
(23, 'ENGL', '1302',        'First Year Writing II',                      3),
-- Core History/Gov
(24, 'HIST', '1301',        'American History I',                         3),
(25, 'HIST', '1302',        'American History II',                        3),
(26, 'GOVT', '2305',        'U.S. Government: Congress, President, and Court', 3),
(27, 'GOVT', '2306',        'U.S. and Texas Constitutions and Politics',  3),
-- Additional Core placeholders
(28, 'CORE', 'LPC-3HR',     'Language, Philosophy and Culture',           3),
(29, 'CORE', 'CA-3HR',      'Creative Arts',                              3),
(30, 'CORE', 'SBS-3HR',     'Social and Behavioral Sciences',             3),
(31, 'CORE', 'WID-3HR',     'Writing in the Disciplines',                 3),
-- Natural Science placeholders
(32, 'NSM',  'LAB-1HR-1',   'Approved Natural Science Lab 1',             1),
(33, 'NSM',  'LAB-1HR-2',   'Approved Natural Science Lab 2',             1),
(34, 'NSM',  'LEC-3HR-1',   'Approved Natural Science Lecture 1',         3),
(35, 'NSM',  'LEC-3HR-2',   'Approved Natural Science Lecture 2',         3),
(36, 'NSM',  'LEC-3HR-3',   'Approved Natural Science Lecture 3',         3),
(37, 'NSM',  'LEC-3HR-4',   'Approved Natural Science Lecture 4',         3);


-- =======================================================
-- 2. DEGREE PROGRAM
-- =======================================================

INSERT INTO degree_program (program_id, name, college, catalog_year_start, catalog_year_end, total_credits_required)
VALUES (1, 'BS Computer Science', 'College of Natural Sciences and Mathematics', 2024, NULL, 120);


-- =======================================================
-- 3. REQUIREMENT GROUPS
-- =======================================================
-- NOTE on CS Core:
--   COSC 4351 and COSC 4353 are OR alternatives — you take ONE not both.
--   We handle this with a course_set so the algorithm knows either satisfies the requirement.

INSERT INTO requirement_group (group_id, program_id, name, rule_type, min_credits, min_courses, parent_group_id)
VALUES
(1, 1, 'CS Core',                'ALL_OF',    NULL, NULL, NULL),
(2, 1, 'CS Advanced Electives',  'MIN_CREDITS', 15,  NULL, NULL),
(3, 1, 'Math Requirements',      'ALL_OF',    NULL, NULL, NULL),
(4, 1, 'Core Communication',     'ALL_OF',    NULL, NULL, NULL),
(5, 1, 'Core History and Gov',   'ALL_OF',    NULL, NULL, NULL),
(6, 1, 'Additional Core',        'ALL_OF',    NULL, NULL, NULL),
(7, 1, 'Natural Science',        'ALL_OF',    NULL, NULL, NULL);


-- =======================================================
-- 4. COURSE SET
-- COSC 4351 / COSC 4353 are OR alternatives.
-- Student must take one of them — handled via course_set + CHOOSE_N logic.
-- Similarly MATH 2318 / MATH 3321 are OR alternatives for the math requirement.
-- =======================================================

INSERT INTO course_set (course_set_id, name) VALUES
(1, 'Software Engineering OR Software Design'),  -- 4351 or 4353
(2, 'Linear Algebra OR Engineering Mathematics'); -- 2318 or 3321


INSERT INTO course_set_course (course_set_id, course_id) VALUES
(1, 9),   -- COSC 4351
(1, 38),  -- COSC 4353
(2, 19),  -- MATH 2318
(2, 20);  -- MATH 3321


-- =======================================================
-- 5. REQUIREMENT ITEMS
-- =======================================================

INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade)
VALUES
-- CS Core — fixed courses
(1, 1,    NULL, TRUE, 'C'),  -- COSC 1336
(1, 2,    NULL, TRUE, 'C'),  -- COSC 1437
(1, 3,    NULL, TRUE, 'C'),  -- COSC 2436
(1, 4,    NULL, TRUE, 'C'),  -- COSC 2425
(1, 5,    NULL, TRUE, 'C'),  -- COSC 3320
(1, 6,    NULL, TRUE, 'C'),  -- COSC 3340
(1, 7,    NULL, TRUE, 'C'),  -- COSC 3360
(1, 8,    NULL, TRUE, 'C'),  -- COSC 3380
-- CS Core — 4351 OR 4353 (course_set 1, choose 1)
(1, NULL, 1,    TRUE, 'C'),  -- COSC 4351 OR COSC 4353
-- CS Advanced Electives
(2, 10,   NULL, TRUE, 'C'),
(2, 11,   NULL, TRUE, 'C'),
(2, 12,   NULL, TRUE, 'C'),
(2, 13,   NULL, TRUE, 'C'),
(2, 14,   NULL, TRUE, 'C'),
-- Math — fixed courses
(3, 15,   NULL, TRUE, 'C'),  -- MATH 2413
(3, 16,   NULL, TRUE, 'C'),  -- MATH 2414
(3, 17,   NULL, TRUE, 'C'),  -- MATH 2305
(3, 21,   NULL, TRUE, 'C'),  -- MATH 3339
-- Math — 2318 OR 3321 (course_set 2, choose 1)
(3, NULL, 2,    TRUE, 'C'),  -- MATH 2318 OR MATH 3321
-- Core Communication
(4, 22,   NULL, TRUE, 'C'),  -- ENGL 1301
(4, 23,   NULL, TRUE, 'C'),  -- ENGL 1302
-- Core History/Gov
(5, 24,   NULL, TRUE, 'C'),  -- HIST 1301
(5, 25,   NULL, TRUE, 'C'),  -- HIST 1302
(5, 26,   NULL, TRUE, 'C'),  -- GOVT 2305
(5, 27,   NULL, TRUE, 'C'),  -- GOVT 2306
-- Additional Core
(6, 28,   NULL, TRUE, NULL),
(6, 29,   NULL, TRUE, NULL),
(6, 30,   NULL, TRUE, NULL),
(6, 31,   NULL, TRUE, NULL),
-- Natural Science
(7, 32,   NULL, TRUE, NULL),
(7, 33,   NULL, TRUE, NULL),
(7, 34,   NULL, TRUE, NULL),
(7, 35,   NULL, TRUE, NULL),
(7, 36,   NULL, TRUE, NULL),
(7, 37,   NULL, TRUE, NULL);


-- =======================================================
-- 6. PREREQUISITE RULES + NODES
--
-- Prereq logic per Excel:
--   COSC 1437  → COSC 1336 AND MATH 2413
--   COSC 2436  → COSC 1437 AND MATH 2305 AND MATH 2414
--   COSC 2425  → COSC 1437
--   COSC 3320  → COSC 2436
--   COSC 3340  → COSC 2436
--   COSC 3360  → COSC 2436
--   COSC 3380  → COSC 2436
--   COSC 4351  → COSC 3320  (4353 is an OR alternative at the requirement level, not prereq level)
--   COSC 4353  → COSC 3320  (same)
--   COSC 2305  → MATH 2318 OR MATH 2305
--   MATH 2414  → MATH 2413
--   MATH 2305  → MATH 2413 OR COSC 2305
--   MATH 2318  → MATH 2414 OR MATH 3321
--   MATH 3321  → MATH 2414 OR MATH 2318
--   MATH 3339  → MATH 2414
--   ENGL 1302  → ENGL 1301
--   HIST 1302  → HIST 1301
--   CS Electives → COSC 2436
-- =======================================================

-- ---------------------------------------------------------
-- COSC 1437: COSC 1336 AND MATH 2413
-- AND(root) → COURSE(1336), COURSE(2413)
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(1, 'AND',    NULL, NULL, 0),
(2, 'COURSE', 1,    1,    0),   -- COSC 1336
(3, 'COURSE', 15,   1,    1);   -- MATH 2413

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (1, 2, 'PREREQ', 1);

UPDATE requisite_node SET rule_id = 1 WHERE node_id IN (1,2,3);

-- ---------------------------------------------------------
-- COSC 2436: COSC 1437 AND MATH 2305 AND MATH 2414
-- AND(root) → COURSE(1437), COURSE(2305), COURSE(2414)
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(4, 'AND',    NULL, NULL, 0),
(5, 'COURSE', 2,    4,    0),   -- COSC 1437
(6, 'COURSE', 17,   4,    1),   -- MATH 2305
(7, 'COURSE', 16,   4,    2);   -- MATH 2414

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (2, 3, 'PREREQ', 4);

UPDATE requisite_node SET rule_id = 2 WHERE node_id IN (4,5,6,7);

-- ---------------------------------------------------------
-- COSC 2425: COSC 1437
-- COURSE(root) → 1437
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(8, 'COURSE', 2, NULL, 0);

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (3, 4, 'PREREQ', 8);

UPDATE requisite_node SET rule_id = 3 WHERE node_id = 8;

-- ---------------------------------------------------------
-- COSC 3320: COSC 2436
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(9, 'COURSE', 3, NULL, 0);

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (4, 5, 'PREREQ', 9);

UPDATE requisite_node SET rule_id = 4 WHERE node_id = 9;

-- ---------------------------------------------------------
-- COSC 3340: COSC 2436
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(10, 'COURSE', 3, NULL, 0);

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (5, 6, 'PREREQ', 10);

UPDATE requisite_node SET rule_id = 5 WHERE node_id = 10;

-- ---------------------------------------------------------
-- COSC 3360: COSC 2436
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(11, 'COURSE', 3, NULL, 0);

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (6, 7, 'PREREQ', 11);

UPDATE requisite_node SET rule_id = 6 WHERE node_id = 11;

-- ---------------------------------------------------------
-- COSC 3380: COSC 2436
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(12, 'COURSE', 3, NULL, 0);

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (7, 8, 'PREREQ', 12);

UPDATE requisite_node SET rule_id = 7 WHERE node_id = 12;

-- ---------------------------------------------------------
-- COSC 4351: COSC 3320
-- (4353 is an OR alternative at requirement level via course_set, not at prereq level)
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(13, 'COURSE', 5, NULL, 0);

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (8, 9, 'PREREQ', 13);

UPDATE requisite_node SET rule_id = 8 WHERE node_id = 13;

-- ---------------------------------------------------------
-- COSC 4353: COSC 3320
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(14, 'COURSE', 5, NULL, 0);

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (9, 38, 'PREREQ', 14);

UPDATE requisite_node SET rule_id = 9 WHERE node_id = 14;

-- ---------------------------------------------------------
-- CS Advanced Electives 1-5: all require COSC 2436
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(15, 'COURSE', 3, NULL, 0),
(16, 'COURSE', 3, NULL, 0),
(17, 'COURSE', 3, NULL, 0),
(18, 'COURSE', 3, NULL, 0),
(19, 'COURSE', 3, NULL, 0);

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES
(10, 10, 'PREREQ', 15),
(11, 11, 'PREREQ', 16),
(12, 12, 'PREREQ', 17),
(13, 13, 'PREREQ', 18),
(14, 14, 'PREREQ', 19);

UPDATE requisite_node SET rule_id = 10 WHERE node_id = 15;
UPDATE requisite_node SET rule_id = 11 WHERE node_id = 16;
UPDATE requisite_node SET rule_id = 12 WHERE node_id = 17;
UPDATE requisite_node SET rule_id = 13 WHERE node_id = 18;
UPDATE requisite_node SET rule_id = 14 WHERE node_id = 19;

-- ---------------------------------------------------------
-- MATH 2414: MATH 2413
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(20, 'COURSE', 15, NULL, 0);

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (15, 16, 'PREREQ', 20);

UPDATE requisite_node SET rule_id = 15 WHERE node_id = 20;

-- ---------------------------------------------------------
-- MATH 2305: MATH 2413 OR COSC 2305
-- OR(root) → COURSE(2413), COURSE(2305)
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(21, 'OR',    NULL, NULL, 0),
(22, 'COURSE', 15,  21,   0),  -- MATH 2413
(23, 'COURSE', 18,  21,   1);  -- COSC 2305

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (16, 17, 'PREREQ', 21);

UPDATE requisite_node SET rule_id = 16 WHERE node_id IN (21,22,23);

-- ---------------------------------------------------------
-- COSC 2305: MATH 2318 OR MATH 2305
-- OR(root) → COURSE(2318), COURSE(2305)
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(24, 'OR',    NULL, NULL, 0),
(25, 'COURSE', 19,  24,   0),  -- MATH 2318
(26, 'COURSE', 17,  24,   1);  -- MATH 2305

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (17, 18, 'PREREQ', 24);

UPDATE requisite_node SET rule_id = 17 WHERE node_id IN (24,25,26);

-- ---------------------------------------------------------
-- MATH 2318: MATH 2414 OR MATH 3321
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(27, 'OR',    NULL, NULL, 0),
(28, 'COURSE', 16,  27,   0),  -- MATH 2414
(29, 'COURSE', 20,  27,   1);  -- MATH 3321

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (18, 19, 'PREREQ', 27);

UPDATE requisite_node SET rule_id = 18 WHERE node_id IN (27,28,29);

-- ---------------------------------------------------------
-- MATH 3321: MATH 2414 OR MATH 2318
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(30, 'OR',    NULL, NULL, 0),
(31, 'COURSE', 16,  30,   0),  -- MATH 2414
(32, 'COURSE', 19,  30,   1);  -- MATH 2318

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (19, 20, 'PREREQ', 30);

UPDATE requisite_node SET rule_id = 19 WHERE node_id IN (30,31,32);

-- ---------------------------------------------------------
-- MATH 3339: MATH 2414
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(33, 'COURSE', 16, NULL, 0);

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (20, 21, 'PREREQ', 33);

UPDATE requisite_node SET rule_id = 20 WHERE node_id = 33;

-- ---------------------------------------------------------
-- ENGL 1302: ENGL 1301
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(34, 'COURSE', 22, NULL, 0);

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (21, 23, 'PREREQ', 34);

UPDATE requisite_node SET rule_id = 21 WHERE node_id = 34;

-- ---------------------------------------------------------
-- HIST 1302: HIST 1301
-- ---------------------------------------------------------
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(35, 'COURSE', 24, NULL, 0);

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (22, 25, 'PREREQ', 35);

UPDATE requisite_node SET rule_id = 22 WHERE node_id = 35;

SET FOREIGN_KEY_CHECKS = 1;





