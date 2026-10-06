-- =======================================================
-- V7: Finance Bauer Core + per-track Bauer elective slots
-- ---------------------------------------------------------
-- Goal: a Standard-track Finance plan totals exactly 120 cr
--       using NAMED requirements (no auto-generated
--       "Free Elective" placeholders). Other tracks land
--       at 120 cr too via per-track elective groups.
--
-- course_id continues from 143
-- group_id  continues from 24
-- =======================================================

SET FOREIGN_KEY_CHECKS = 0;

-- =======================================================
-- 1. RENAME existing Standard-track placeholders so the
--    UI shows "Advanced Finance Elective" instead of
--    the cryptic "STD-ELEC-N" / "Finance Elective N".
-- =======================================================
UPDATE course SET subject='FINA', number='ADV-FIN-ELEC-1',
       title='Advanced Finance Elective (3000-4000 level FINA)' WHERE course_id=138;
UPDATE course SET subject='FINA', number='ADV-FIN-ELEC-2',
       title='Advanced Finance Elective (3000-4000 level FINA)' WHERE course_id=139;
UPDATE course SET subject='FINA', number='ADV-FIN-ELEC-3',
       title='Advanced Finance Elective (3000-4000 level FINA)' WHERE course_id=140;
UPDATE course SET subject='FINA', number='ADV-FIN-ELEC-4',
       title='Advanced Finance Elective (3000-4000 level FINA)' WHERE course_id=141;


-- =======================================================
-- 2. TRIM Foundation + Additional Core to match the
--    actual Bauer BBA Finance plan (which lists ONLY
--    MATH 1324 and only LPC + CA from CORE, not SBS/WID).
--    This trims 9 cr so the new Bauer Core (30 cr) +
--    Bauer Electives (variable) hit 120 cr exactly.
-- =======================================================
DELETE FROM requirement_item WHERE group_id=12 AND course_id=87;        -- drop MATH 1314 from Foundation
DELETE FROM requirement_item WHERE group_id=23 AND course_id IN (30,31); -- drop SBS, WID from Additional Core

-- ECON 2302 prereq was MATH 1314 -> change to MATH 1324 (course_id 88)
-- (Bauer's MATH 1324 satisfies the same prereq path.)
UPDATE requisite_node SET course_id=88 WHERE node_id=46;


-- =======================================================
-- 3. NEW Bauer Common Body of Knowledge courses
--    Required for ALL Finance majors regardless of track.
-- =======================================================
INSERT INTO course (course_id, subject, number, title, credits) VALUES
(144, 'BCIS', '1305', 'Business Computer Applications',                            3),
(145, 'BUSI', '1301', 'Business Principles',                                       3),
(146, 'BUSI', '2305', 'Business Statistics',                                       3),
(147, 'BUSI', '3302', 'Connecting Bauer to Business',                              3),
(148, 'BUSI', '4350', 'Business Law and Ethics',                                   3),
(149, 'INTB', '3355', 'Global Environment of Business',                            3),
(150, 'MANA', '3335', 'Introduction to Organizational Behavior and Management',    3),
(151, 'MARK', '3336', 'Introduction to Marketing',                                 3),
(152, 'MARK', '3337', 'Professional Selling',                                      3),
(153, 'SCM',  '3301', 'Supply Chain Management Fundamentals',                      3);


-- =======================================================
-- 4. NEW per-track Bauer-elective placeholders.
--    Names are clear about what kind of course is required
--    so the UI never says "Free Elective" until a student
--    has truly satisfied every named requirement.
-- =======================================================
INSERT INTO course (course_id, subject, number, title, credits) VALUES
(154, 'BUSI', 'ADV-BUS-ELEC-1', 'Advanced Business Elective (3000-4000 level)',    3),
(155, 'BUSI', 'ADV-BUS-ELEC-2', 'Advanced Business Elective (3000-4000 level)',    3),
(156, 'BUSI', 'ADV-BUS-ELEC-3', 'Advanced Business Elective (3000-4000 level)',    3),

(157, 'ELEC', 'ADV-ELEC-1',     'Advanced Elective (3000-4000 level)',             3),
(158, 'ELEC', 'ADV-ELEC-2',     'Advanced Elective (3000-4000 level)',             3),
(159, 'ELEC', 'ADV-ELEC-3',     'Advanced Elective (3000-4000 level)',             3),

(160, 'ELEC', 'GEN-ELEC-1',     'General Elective (1000-4000 level)',              3),
(161, 'ELEC', 'GEN-ELEC-2',     'General Elective (1000-4000 level)',              3),
(162, 'ELEC', 'GEN-ELEC-3',     'General Elective (1000-4000 level)',              3),
(163, 'ELEC', 'GEN-ELEC-4',     'General Elective (1000-4000 level)',              3),
(164, 'ELEC', 'GEN-ELEC-5',     'General Elective (1000-4000 level)',              3);


-- =======================================================
-- 5. NEW REQUIREMENT GROUPS
-- =======================================================
INSERT INTO requirement_group (group_id, program_id, name, rule_type, min_credits, min_courses, parent_group_id) VALUES
(25, 2, 'Finance Bauer Core',                                                'ALL_OF', NULL, NULL, NULL),
(26, 2, 'Finance Bauer Electives: Standard',                                 'ALL_OF', NULL, NULL, NULL),
(27, 2, 'Finance Bauer Electives: Real Estate',                              'ALL_OF', NULL, NULL, NULL),
(28, 2, 'Finance Bauer Electives: Personal Financial Planning',              'ALL_OF', NULL, NULL, NULL),
(29, 2, 'Finance Bauer Electives: Corporate Banking and Credit',             'ALL_OF', NULL, NULL, NULL),
(30, 2, 'Finance Bauer Electives: Global Energy Management',                 'ALL_OF', NULL, NULL, NULL),
(31, 2, 'Finance Bauer Electives: Energy Commodities Trading and Consulting','ALL_OF', NULL, NULL, NULL);


-- ---- (25) Finance Bauer Core (30 cr — all Finance majors) ----
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(25, 144, NULL, TRUE, 'C'),  -- BCIS 1305
(25, 145, NULL, TRUE, 'C'),  -- BUSI 1301
(25, 146, NULL, TRUE, 'C'),  -- BUSI 2305
(25, 147, NULL, TRUE, 'C'),  -- BUSI 3302
(25, 148, NULL, TRUE, 'C'),  -- BUSI 4350
(25, 149, NULL, TRUE, 'C'),  -- INTB 3355
(25, 150, NULL, TRUE, 'C'),  -- MANA 3335
(25, 151, NULL, TRUE, 'C'),  -- MARK 3336
(25, 152, NULL, TRUE, 'C'),  -- MARK 3337
(25, 153, NULL, TRUE, 'C');  -- SCM 3301


-- ---- (26) Standard track electives (21 cr) ----
-- Per the official Bauer BBA Finance (Standard) degree plan:
-- 2 Adv Bus Elec + 2 Adv Elec + 3 Gen Elec
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(26, 154, NULL, TRUE, NULL),  -- ADV-BUS-ELEC-1
(26, 155, NULL, TRUE, NULL),  -- ADV-BUS-ELEC-2
(26, 157, NULL, TRUE, NULL),  -- ADV-ELEC-1
(26, 158, NULL, TRUE, NULL),  -- ADV-ELEC-2
(26, 160, NULL, TRUE, NULL),  -- GEN-ELEC-1
(26, 161, NULL, TRUE, NULL),  -- GEN-ELEC-2
(26, 162, NULL, TRUE, NULL);  -- GEN-ELEC-3


-- ---- (27) Real Estate track electives (21 cr) ----
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(27, 154, NULL, TRUE, NULL),
(27, 155, NULL, TRUE, NULL),
(27, 157, NULL, TRUE, NULL),
(27, 158, NULL, TRUE, NULL),
(27, 160, NULL, TRUE, NULL),
(27, 161, NULL, TRUE, NULL),
(27, 162, NULL, TRUE, NULL);


-- ---- (28) Personal Financial Planning track electives (18 cr) ----
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(28, 154, NULL, TRUE, NULL),
(28, 155, NULL, TRUE, NULL),
(28, 157, NULL, TRUE, NULL),
(28, 158, NULL, TRUE, NULL),
(28, 160, NULL, TRUE, NULL),
(28, 161, NULL, TRUE, NULL);


-- ---- (29) Corporate Banking and Credit track electives (15 cr) ----
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(29, 154, NULL, TRUE, NULL),
(29, 157, NULL, TRUE, NULL),
(29, 158, NULL, TRUE, NULL),
(29, 160, NULL, TRUE, NULL),
(29, 161, NULL, TRUE, NULL);


-- ---- (30) Global Energy Management track electives (6 cr) ----
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(30, 157, NULL, TRUE, NULL),
(30, 160, NULL, TRUE, NULL);


-- ---- (31) Energy Commodities Trading and Consulting electives (15 cr) ----
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(31, 154, NULL, TRUE, NULL),
(31, 157, NULL, TRUE, NULL),
(31, 158, NULL, TRUE, NULL),
(31, 160, NULL, TRUE, NULL),
(31, 161, NULL, TRUE, NULL);


SET FOREIGN_KEY_CHECKS = 1;
