-- =======================================================
-- V5: Finance Degree Plan — Base Courses & Foundation
-- Run after V4__capstone_minor_electives.sql
-- IDs continue from: course_id=80, node_id=41, rule_id=28,
--                    group_id=11, program_id=1
-- =======================================================

SET FOREIGN_KEY_CHECKS = 0;

-- =======================================================
-- 1. FINANCE DEGREE PROGRAM
-- program_id = 2
-- =======================================================

INSERT INTO degree_program (program_id, name, college, catalog_year_start, catalog_year_end, total_credits_required)
VALUES (2, 'BBA Finance', 'C.T. Bauer College of Business', 2024, NULL, 120);


-- =======================================================
-- 2. FOUNDATION / PREREQUISITE COURSES
-- These are required before any Finance upper-div courses.
-- Many are shared across all tracks.
-- =======================================================

INSERT INTO course (course_id, subject, number, title, credits) VALUES

-- Business Foundation (required before most FINA 4XXX)
(81,  'ACCT', '2301', 'Principles of Financial Accounting',      3),
(82,  'ACCT', '2302', 'Principles of Managerial Accounting',     3),
(83,  'FINA', '3332', 'Principles of Financial Management',      3),
(84,  'STAT', '3331', 'Statistical Analysis for Business Apps I',3),
(85,  'ECON', '2301', 'Principles of Macroeconomics',            3),
(86,  'ECON', '2302', 'Principles of Microeconomics',            3),
(87,  'MATH', '1314', 'College Algebra',                         3),
(88,  'MATH', '1324', 'Math for Business and Social Sciences I', 3),

-- Required for ALL Finance majors (both tracks)
(89,  'FINA', '4320', 'Investment Management',                   3),
(90,  'FINA', '4330', 'Corporate Finance',                       3),

-- ACCT courses needed for CBC track
(91,  'ACCT', '3366', 'Financial Reporting Frameworks',          3),
(92,  'ACCT', '3367', 'Intermediate Accounting I',               3),
(93,  'ACCT', '3368', 'Intermediate Accounting II',              3),

-- Real Estate track courses
(94,  'FINA', '4380', 'Real Estate Finance and Investment',      3),
(95,  'FINA', '4383', 'Real Estate Market Research and Valuation',3),
(96,  'FINA', '4382', 'Developing a Real Estate Project',        3),  -- Fall only
(97,  'FINA', '4397', 'Asset Management',                        3),  -- Spring only (RE track version)

-- PFP Track courses
(98,  'FINA', '4352', 'Financial Planning for Professionals',    3),
(99,  'FINA', '4325', 'Retirement and Estate Planning',          3),
(100, 'FINA', '4353', 'Practicum in Personal Financial Planning',3),
(101, 'FINA', '4354', 'Risk Management',                         3),
(102, 'ACCT', '4331', 'Federal Income Tax',                      3),

-- CBC Track courses
(103, 'FINA', '4341', 'Commercial Bank Management',              3),
(104, 'FINA', '4342', 'Financial Evaluation of Corporate Reports',3),  -- Spring only
(105, 'FINA', '4343', 'Credit Analysis',                         3),

-- GEM Track courses
(106, 'FINA', '4350', 'Derivatives I: Options',                  3),
(107, 'FINA', '4351', 'Derivatives II: Futures and Swaps',       3),
(108, 'FINA', '4357', 'Commercial Liability',                    3),
(109, 'FINA', '4358', 'Commercial Property',                     3),
(110, 'FINA', '4359', 'Energy Insurance and Risk Management',    3),
(111, 'FINA', '4360', 'International Financial Management',      3),
(112, 'FINA', '4370', 'Energy Trading',                          3),
(113, 'FINA', '4371', 'Energy Value Chain',                      3),
(114, 'FINA', '4372', 'Upstream Economics',                      3),
(115, 'FINA', '4373', 'Petrochemical and Refining Economics',    3),
(116, 'FINA', '4374', 'Energy Analysis',                         3),
(117, 'FINA', '4376', 'Energy Trading Systems',                  3),

-- GEM Advanced Electives
(118, 'ACCT', '3385', 'Intermediate Cost Accounting',            3),  -- placeholder label used in catalog
(119, 'SCM',  '4302', 'Energy Supply Chain',                     3),
(120, 'SCM',  '4311', 'Project Management',                      3),
(121, 'TECH', '4310', 'Future of Energy and Environment',        3),
(122, 'ECON', '3385', 'Economics of Energy',                     3),
(123, 'HIST', '3349', 'War, Globalization and Terror',           3),
(124, 'ENRG', '3310', 'Energy and Sustainability',               3),
(125, 'ENRG', '4320', 'Case Studies in Energy and Sustainability',3),

-- ECT&C Track courses (Energy)
(126, 'FINA', '4396', 'Finance Internship / Experiential Learning',3),

-- GEM elective placeholders (choose 12 cr from GEM list)
(127, 'FINA', 'GEM-ELEC-1', 'GEM Finance Elective 1',           3),
(128, 'FINA', 'GEM-ELEC-2', 'GEM Finance Elective 2',           3),
(129, 'FINA', 'GEM-ELEC-3', 'GEM Finance Elective 3',           3),
(130, 'FINA', 'GEM-ELEC-4', 'GEM Finance Elective 4',           3),

-- GEM advanced elective placeholders (choose 6 cr)
(131, 'FINA', 'GEM-ADV-1',  'GEM Advanced Elective 1',          3),
(132, 'FINA', 'GEM-ADV-2',  'GEM Advanced Elective 2',          3),

-- ECT&C elective placeholders (choose 5 courses)
(133, 'FINA', 'ECTC-ELEC-1','ECT&C Elective 1',                 3),
(134, 'FINA', 'ECTC-ELEC-2','ECT&C Elective 2',                 3),
(135, 'FINA', 'ECTC-ELEC-3','ECT&C Elective 3',                 3),
(136, 'FINA', 'ECTC-ELEC-4','ECT&C Elective 4',                 3),
(137, 'FINA', 'ECTC-ELEC-5','ECT&C Elective 5',                 3),

-- Standard track elective placeholders (choose 12 cr upper-div finance)
(138, 'FINA', 'STD-ELEC-1', 'Finance Elective 1',               3),
(139, 'FINA', 'STD-ELEC-2', 'Finance Elective 2',               3),
(140, 'FINA', 'STD-ELEC-3', 'Finance Elective 3',               3),
(141, 'FINA', 'STD-ELEC-4', 'Finance Elective 4',               3);


-- =======================================================
-- 3. PREREQUISITE RULES — FOUNDATION COURSES
-- node_id continues from 41, rule_id from 28
-- =======================================================

-- ACCT 2302 → ACCT 2301
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(42, 'COURSE', 81, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (29, 82, 'PREREQ', 42);
UPDATE requisite_node SET rule_id = 29 WHERE node_id = 42;

-- FINA 3332 → ACCT 2301 AND MATH 1324
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(43, 'AND',    NULL, NULL, 0),
(44, 'COURSE', 81,   43,   0),   -- ACCT 2301
(45, 'COURSE', 88,   43,   1);   -- MATH 1324
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (30, 83, 'PREREQ', 43);
UPDATE requisite_node SET rule_id = 30 WHERE node_id IN (43,44,45);

-- ECON 2302 → MATH 1314
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(46, 'COURSE', 87, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (31, 86, 'PREREQ', 46);
UPDATE requisite_node SET rule_id = 31 WHERE node_id = 46;

-- ACCT 3366 → ACCT 2301 AND ACCT 2302
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(47, 'AND',    NULL, NULL, 0),
(48, 'COURSE', 81,   47,   0),   -- ACCT 2301
(49, 'COURSE', 82,   47,   1);   -- ACCT 2302
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (32, 91, 'PREREQ', 47);
UPDATE requisite_node SET rule_id = 32 WHERE node_id IN (47,48,49);

-- ACCT 3367 → ACCT 3366
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(50, 'COURSE', 91, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (33, 92, 'PREREQ', 50);
UPDATE requisite_node SET rule_id = 33 WHERE node_id = 50;

-- ACCT 3368 → ACCT 3367
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(51, 'COURSE', 92, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (34, 93, 'PREREQ', 51);
UPDATE requisite_node SET rule_id = 34 WHERE node_id = 51;

-- Standard prereq for most FINA 4XXX: FINA 3332 AND STAT 3331 AND ECON 2302
-- Reusable pattern — each course gets its own nodes
-- FINA 4320
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(52, 'AND',    NULL, NULL, 0),
(53, 'COURSE', 83,   52,   0),   -- FINA 3332
(54, 'COURSE', 84,   52,   1),   -- STAT 3331
(55, 'COURSE', 86,   52,   2);   -- ECON 2302
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (35, 89, 'PREREQ', 52);
UPDATE requisite_node SET rule_id = 35 WHERE node_id IN (52,53,54,55);

-- FINA 4330
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(56, 'AND',    NULL, NULL, 0),
(57, 'COURSE', 83,   56,   0),
(58, 'COURSE', 84,   56,   1),
(59, 'COURSE', 86,   56,   2);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (36, 90, 'PREREQ', 56);
UPDATE requisite_node SET rule_id = 36 WHERE node_id IN (56,57,58,59);

-- FINA 4380
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(60, 'AND',    NULL, NULL, 0),
(61, 'COURSE', 83,   60,   0),
(62, 'COURSE', 84,   60,   1),
(63, 'COURSE', 86,   60,   2);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (37, 94, 'PREREQ', 60);
UPDATE requisite_node SET rule_id = 37 WHERE node_id IN (60,61,62,63);

-- FINA 4383 → FINA 4330 AND FINA 4380
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(64, 'AND',    NULL, NULL, 0),
(65, 'COURSE', 90,   64,   0),   -- FINA 4330
(66, 'COURSE', 94,   64,   1);   -- FINA 4380
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (38, 95, 'PREREQ', 64);
UPDATE requisite_node SET rule_id = 38 WHERE node_id IN (64,65,66);

-- FINA 4382 → FINA 4330 AND FINA 4380
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(67, 'AND',    NULL, NULL, 0),
(68, 'COURSE', 90,   67,   0),
(69, 'COURSE', 94,   67,   1);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (39, 96, 'PREREQ', 67);
UPDATE requisite_node SET rule_id = 39 WHERE node_id IN (67,68,69);

-- FINA 4397 (Asset Mgmt RE version) → FINA 4330 AND FINA 4380
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(70, 'AND',    NULL, NULL, 0),
(71, 'COURSE', 90,   70,   0),
(72, 'COURSE', 94,   70,   1);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (40, 97, 'PREREQ', 70);
UPDATE requisite_node SET rule_id = 40 WHERE node_id IN (70,71,72);

-- FINA 4352 → FINA 3332 AND STAT 3331 AND ECON 2302
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(73, 'AND',    NULL, NULL, 0),
(74, 'COURSE', 83,   73,   0),
(75, 'COURSE', 84,   73,   1),
(76, 'COURSE', 86,   73,   2);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (41, 98, 'PREREQ', 73);
UPDATE requisite_node SET rule_id = 41 WHERE node_id IN (73,74,75,76);

-- FINA 4325 → FINA 3332 AND STAT 3331 AND ECON 2302 AND FINA 4352
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(77, 'AND',    NULL, NULL, 0),
(78, 'COURSE', 83,   77,   0),
(79, 'COURSE', 84,   77,   1),
(80, 'COURSE', 86,   77,   2),
(81, 'COURSE', 98,   77,   3);   -- FINA 4352
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (42, 99, 'PREREQ', 77);
UPDATE requisite_node SET rule_id = 42 WHERE node_id IN (77,78,79,80,81);

-- FINA 4353 → FINA 4352
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(82, 'COURSE', 98, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (43, 100, 'PREREQ', 82);
UPDATE requisite_node SET rule_id = 43 WHERE node_id = 82;

-- FINA 4354 → FINA 3332 AND STAT 3331 AND ECON 2302
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(83, 'AND',    NULL, NULL, 0),
(84, 'COURSE', 83,   83,   0),
(85, 'COURSE', 84,   83,   1),
(86, 'COURSE', 86,   83,   2);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (44, 101, 'PREREQ', 83);
UPDATE requisite_node SET rule_id = 44 WHERE node_id IN (83,84,85,86);

-- FINA 4341 → FINA 3332 AND STAT 3331 AND ECON 2302
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(87, 'AND',    NULL, NULL, 0),
(88, 'COURSE', 83,   87,   0),
(89, 'COURSE', 84,   87,   1),
(90, 'COURSE', 86,   87,   2);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (45, 103, 'PREREQ', 87);
UPDATE requisite_node SET rule_id = 45 WHERE node_id IN (87,88,89,90);

-- FINA 4342 → FINA 3332 AND STAT 3331 AND ECON 2302
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(91, 'AND',    NULL, NULL, 0),
(92, 'COURSE', 83,   91,   0),
(93, 'COURSE', 84,   91,   1),
(94, 'COURSE', 86,   91,   2);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (46, 104, 'PREREQ', 91);
UPDATE requisite_node SET rule_id = 46 WHERE node_id IN (91,92,93,94);

-- FINA 4343 → FINA 3332 AND STAT 3331 AND ECON 2302
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(95, 'AND',    NULL, NULL, 0),
(96, 'COURSE', 83,   95,   0),
(97, 'COURSE', 84,   95,   1),
(98, 'COURSE', 86,   95,   2);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (47, 105, 'PREREQ', 95);
UPDATE requisite_node SET rule_id = 47 WHERE node_id IN (95,96,97,98);

-- FINA 4371 → FINA 3332 AND STAT 3331 AND ECON 2302
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(99,  'AND',    NULL, NULL, 0),
(100, 'COURSE', 83,   99,   0),
(101, 'COURSE', 84,   99,   1),
(102, 'COURSE', 86,   99,   2);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (48, 113, 'PREREQ', 99);
UPDATE requisite_node SET rule_id = 48 WHERE node_id IN (99,100,101,102);

-- FINA 4372 → FINA 3332 AND STAT 3331 AND ECON 2302
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(103, 'AND',    NULL, NULL, 0),
(104, 'COURSE', 83,   103,  0),
(105, 'COURSE', 84,   103,  1),
(106, 'COURSE', 86,   103,  2);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (49, 114, 'PREREQ', 103);
UPDATE requisite_node SET rule_id = 49 WHERE node_id IN (103,104,105,106);

-- FINA 4370, 4373, 4374, 4359, 4360, 4350, 4351, 4357, 4358, 4376
-- All share: FINA 3332 AND STAT 3331 AND ECON 2302
-- Generate a node group per course
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(107, 'AND', NULL, NULL, 0),(108, 'COURSE', 83, 107, 0),(109, 'COURSE', 84, 107, 1),(110, 'COURSE', 86, 107, 2), -- 4370
(111, 'AND', NULL, NULL, 0),(112, 'COURSE', 83, 111, 0),(113, 'COURSE', 84, 111, 1),(114, 'COURSE', 86, 111, 2), -- 4373
(115, 'AND', NULL, NULL, 0),(116, 'COURSE', 83, 115, 0),(117, 'COURSE', 84, 115, 1),(118, 'COURSE', 86, 115, 2), -- 4374
(119, 'AND', NULL, NULL, 0),(120, 'COURSE', 83, 119, 0),(121, 'COURSE', 84, 119, 1),(122, 'COURSE', 86, 119, 2), -- 4359
(123, 'AND', NULL, NULL, 0),(124, 'COURSE', 83, 123, 0),(125, 'COURSE', 84, 123, 1),(126, 'COURSE', 86, 123, 2), -- 4360
(127, 'AND', NULL, NULL, 0),(128, 'COURSE', 83, 127, 0),(129, 'COURSE', 84, 127, 1),(130, 'COURSE', 86, 127, 2), -- 4350
(131, 'AND', NULL, NULL, 0),(132, 'COURSE', 83, 131, 0),(133, 'COURSE', 84, 131, 1),(134, 'COURSE', 86, 131, 2), -- 4351
(135, 'AND', NULL, NULL, 0),(136, 'COURSE', 83, 135, 0),(137, 'COURSE', 84, 135, 1),(138, 'COURSE', 86, 135, 2), -- 4357
(139, 'AND', NULL, NULL, 0),(140, 'COURSE', 83, 139, 0),(141, 'COURSE', 84, 139, 1),(142, 'COURSE', 86, 139, 2), -- 4358
(143, 'AND', NULL, NULL, 0),(144, 'COURSE', 83, 143, 0),(145, 'COURSE', 84, 143, 1),(146, 'COURSE', 86, 143, 2); -- 4376

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES
(50, 112, 'PREREQ', 107),  -- FINA 4370
(51, 115, 'PREREQ', 111),  -- FINA 4373
(52, 116, 'PREREQ', 115),  -- FINA 4374
(53, 110, 'PREREQ', 119),  -- FINA 4359
(54, 111, 'PREREQ', 123),  -- FINA 4360
(55, 106, 'PREREQ', 127),  -- FINA 4350
(56, 107, 'PREREQ', 131),  -- FINA 4351
(57, 108, 'PREREQ', 135),  -- FINA 4357
(58, 109, 'PREREQ', 139),  -- FINA 4358
(59, 117, 'PREREQ', 143);  -- FINA 4376

UPDATE requisite_node SET rule_id = 50 WHERE node_id IN (107,108,109,110);
UPDATE requisite_node SET rule_id = 51 WHERE node_id IN (111,112,113,114);
UPDATE requisite_node SET rule_id = 52 WHERE node_id IN (115,116,117,118);
UPDATE requisite_node SET rule_id = 53 WHERE node_id IN (119,120,121,122);
UPDATE requisite_node SET rule_id = 54 WHERE node_id IN (123,124,125,126);
UPDATE requisite_node SET rule_id = 55 WHERE node_id IN (127,128,129,130);
UPDATE requisite_node SET rule_id = 56 WHERE node_id IN (131,132,133,134);
UPDATE requisite_node SET rule_id = 57 WHERE node_id IN (135,136,137,138);
UPDATE requisite_node SET rule_id = 58 WHERE node_id IN (139,140,141,142);
UPDATE requisite_node SET rule_id = 59 WHERE node_id IN (143,144,145,146);

-- GEM elective placeholders → FINA 3332
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(147, 'COURSE', 83, NULL, 0),
(148, 'COURSE', 83, NULL, 0),
(149, 'COURSE', 83, NULL, 0),
(150, 'COURSE', 83, NULL, 0),
(151, 'COURSE', 83, NULL, 0),
(152, 'COURSE', 83, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES
(60, 127, 'PREREQ', 147),
(61, 128, 'PREREQ', 148),
(62, 129, 'PREREQ', 149),
(63, 130, 'PREREQ', 150),
(64, 131, 'PREREQ', 151),
(65, 132, 'PREREQ', 152);
UPDATE requisite_node SET rule_id = 60 WHERE node_id = 147;
UPDATE requisite_node SET rule_id = 61 WHERE node_id = 148;
UPDATE requisite_node SET rule_id = 62 WHERE node_id = 149;
UPDATE requisite_node SET rule_id = 63 WHERE node_id = 150;
UPDATE requisite_node SET rule_id = 64 WHERE node_id = 151;
UPDATE requisite_node SET rule_id = 65 WHERE node_id = 152;

SET FOREIGN_KEY_CHECKS = 1;