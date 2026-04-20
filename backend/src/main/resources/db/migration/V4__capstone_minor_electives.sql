-- =======================================================
-- V4: Capstone/Minor, Math Minor Courses, Free Electives
-- =======================================================

SET FOREIGN_KEY_CHECKS = 0;

-- =======================================================
-- 1. ADD capstone_choice COLUMN TO STUDENT (idempotent)
-- Note: V1 already creates this column; the IF NOT EXISTS
-- guard lets V4 run cleanly against fresh databases too.
-- =======================================================
ALTER TABLE student ADD COLUMN IF NOT EXISTS capstone_choice VARCHAR(20) DEFAULT 'SENIOR_SE';

-- =======================================================
-- 2. NEW COURSES
-- =======================================================

-- CS Senior Sequence: Software Engineering
INSERT INTO course (course_id, subject, number, title, credits) VALUES
(39, 'COSC', 'CAP-SE-1', 'CS Capstone #1 (Software Engineering)', 3),
(40, 'COSC', 'CAP-SE-2', 'CS Capstone #2 (Software Engineering)', 3);

-- CS Senior Sequence: Data Science
INSERT INTO course (course_id, subject, number, title, credits) VALUES
(41, 'COSC', 'CAP-DS-1', 'CS Capstone #1 (Data Science)', 3),
(42, 'COSC', 'CAP-DS-2', 'CS Capstone #2 (Data Science)', 3);

-- Math Minor courses (only ones not already in the DB)
INSERT INTO course (course_id, subject, number, title, credits) VALUES
(43, 'MATH', '2415', 'Calculus III',                    4),
(44, 'MATH', '3325', 'Transition to Advanced Math',     3),
(45, 'MATH', '3330', 'Abstract Algebra',                3),
(46, 'MATH', '3331', 'Differential Equations',          3),
(47, 'MATH', '3333', 'Intermediate Analysis',           3),
(48, 'MATH', '3335', 'Vector Analysis',                 3),
(49, 'MATH', '3338', 'Probability',                     3),
(50, 'MATH', '3334', 'Introduction to Functions of a Complex Variable', 3),
(51, 'MATH', '3336', 'Numerical Methods',               3),
(52, 'MATH', '3340', 'Intro to Stochastic Models',      3),
(53, 'MATH', '3363', 'Partial Differential Equations',  3),
(54, 'MATH', '3364', 'Complex Analysis',                3),
(55, 'MATH', '3379', 'Applications of Abstract Algebra', 3),
(56, 'MATH', '4309', 'Mathematical Biology',            3),
(57, 'MATH', '4310', 'Biostatistics',                   3),
(58, 'MATH', '4315', 'Graph Theory',                    3),
(59, 'MATH', '4320', 'Stochastic Processes',            3),
(60, 'MATH', '4322', 'Data Science & Machine Learning', 3),
(61, 'MATH', '4323', 'Statistical Learning',            3),
(62, 'MATH', '4331', 'Real Analysis I',                 3),
(63, 'MATH', '4335', 'Partial Differential Equations I', 3),
(64, 'MATH', '4350', 'Applied Math',                    3),
(65, 'MATH', '4351', 'Optimization',                    3),
(66, 'MATH', '4355', 'Dynamical Systems',               3),
(67, 'MATH', '4362', 'Numerical Methods for ODEs',      3),
(68, 'MATH', '4364', 'Numerical Analysis',              3),
(69, 'MATH', '4365', 'Numerical Linear Algebra Methods', 3),
(70, 'MATH', '4366', 'Numerical Linear Algebra',        3),
(71, 'MATH', '4377', 'Advanced Linear Algebra I',       3),
(72, 'MATH', '4378', 'Advanced Linear Algebra II',      3),
(73, 'MATH', '4380', 'Financial Mathematics',           3),
(74, 'MATH', '4383', 'Number Theory & Cryptography',    3),
(75, 'MATH', '2312', 'Pre-Calculus',                    3);

-- Math Minor Placeholders (the 2 additional courses a student must pick)
INSERT INTO course (course_id, subject, number, title, credits) VALUES
(76, 'MATH', 'MINOR-3XXX', 'Math Minor Elective (3000+ Level)', 3),
(77, 'MATH', 'MINOR-4XXX', 'Math Minor Elective (4000+ Level)', 3);

-- Free Elective placeholders (need 8 credits to reach 120)
INSERT INTO course (course_id, subject, number, title, credits) VALUES
(78, 'ELEC', 'FREE-3HR-1', 'Free Elective 1', 3),
(79, 'ELEC', 'FREE-3HR-2', 'Free Elective 2', 3),
(80, 'ELEC', 'FREE-2HR',   'Free Elective 3', 2);


-- =======================================================
-- 3. REQUIREMENT GROUPS
-- =======================================================

-- Capstone: Software Engineering (group_id = 8)
INSERT INTO requirement_group (group_id, program_id, name, rule_type, min_credits, min_courses, parent_group_id)
VALUES (8, 1, 'CS Capstone (Software Engineering)', 'ALL_OF', NULL, NULL, NULL);

-- Capstone: Data Science (group_id = 9)
INSERT INTO requirement_group (group_id, program_id, name, rule_type, min_credits, min_courses, parent_group_id)
VALUES (9, 1, 'CS Capstone (Data Science)', 'ALL_OF', NULL, NULL, NULL);

-- Math Minor (group_id = 10)
INSERT INTO requirement_group (group_id, program_id, name, rule_type, min_credits, min_courses, parent_group_id)
VALUES (10, 1, 'Math Minor', 'ALL_OF', NULL, NULL, NULL);

-- Free Electives (group_id = 11)
INSERT INTO requirement_group (group_id, program_id, name, rule_type, min_credits, min_courses, parent_group_id)
VALUES (11, 1, 'Free Electives', 'ALL_OF', NULL, NULL, NULL);


-- =======================================================
-- 4. REQUIREMENT ITEMS
-- =======================================================

-- Capstone SE items
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(8, 39, NULL, TRUE, 'C'),   -- CAP-SE-1
(8, 40, NULL, TRUE, 'C');   -- CAP-SE-2

-- Capstone DS items
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(9, 41, NULL, TRUE, 'C'),   -- CAP-DS-1
(9, 42, NULL, TRUE, 'C');   -- CAP-DS-2

-- Math Minor items (the 2 additional math courses the student needs)
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(10, 76, NULL, TRUE, 'C'),  -- Math Minor 3000+ level elective
(10, 77, NULL, TRUE, 'C');  -- Math Minor 4000+ level elective

-- Free Elective items
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(11, 78, NULL, TRUE, NULL),  -- Free Elective 1 (3cr)
(11, 79, NULL, TRUE, NULL),  -- Free Elective 2 (3cr)
(11, 80, NULL, TRUE, NULL);  -- Free Elective 3 (2cr)


-- =======================================================
-- 5. PREREQUISITE RULES FOR CAPSTONE COURSES
-- =======================================================

-- CAP-SE-1 requires COSC 3320 (Algorithms)
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(36, 'COURSE', 5, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (23, 39, 'PREREQ', 36);
UPDATE requisite_node SET rule_id = 23 WHERE node_id = 36;

-- CAP-SE-2 requires CAP-SE-1
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(37, 'COURSE', 39, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (24, 40, 'PREREQ', 37);
UPDATE requisite_node SET rule_id = 24 WHERE node_id = 37;

-- CAP-DS-1 requires COSC 3320 (Algorithms)
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(38, 'COURSE', 5, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (25, 41, 'PREREQ', 38);
UPDATE requisite_node SET rule_id = 25 WHERE node_id = 38;

-- CAP-DS-2 requires CAP-DS-1
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(39, 'COURSE', 41, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (26, 42, 'PREREQ', 39);
UPDATE requisite_node SET rule_id = 26 WHERE node_id = 39;

-- Math Minor 3000+ elective requires MATH 2414
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(40, 'COURSE', 16, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (27, 76, 'PREREQ', 40);
UPDATE requisite_node SET rule_id = 27 WHERE node_id = 40;

-- Math Minor 4000+ elective requires MATH 3339 (Statistics)
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(41, 'COURSE', 21, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id)
VALUES (28, 77, 'PREREQ', 41);
UPDATE requisite_node SET rule_id = 28 WHERE node_id = 41;

SET FOREIGN_KEY_CHECKS = 1;
