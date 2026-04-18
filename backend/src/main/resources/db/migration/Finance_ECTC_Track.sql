-- =======================================================
-- V11: Finance — Energy Companies Track & Certificate (ECT&C)
-- Required: FINA 4320, 4330, 4371
-- Choose 5 courses from approved ECT&C electives list
-- Complete at least 1 experiential learning activity (FINA 4396/4398)
-- Admission requires: Junior standing, 3.0+ GPA, program director approval
-- =======================================================

SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO course (course_id, subject, number, title, credits) VALUES
(142, 'FINA', '4398', 'Finance Research Project (ECT&C)', 3);

-- FINA 4398 prereq → FINA 3332
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(160, 'COURSE', 83, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (71, 142, 'PREREQ', 160);
UPDATE requisite_node SET rule_id = 71 WHERE node_id = 160;

INSERT INTO requirement_group (group_id, program_id, name, rule_type, min_credits, min_courses, parent_group_id)
VALUES
(29, 2, 'ECT&C: Business Foundation',       'ALL_OF',   NULL, NULL, NULL),
(30, 2, 'ECT&C: Required Core',             'ALL_OF',   NULL, NULL, NULL),
(31, 2, 'ECT&C: Required Track Courses',    'ALL_OF',   NULL, NULL, NULL),
(32, 2, 'ECT&C: Elective Courses',          'CHOOSE_N', NULL, 5,    NULL),
(33, 2, 'ECT&C: Experiential Learning',     'CHOOSE_N', NULL, 1,    NULL);

-- Business Foundation
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(29, 81,  NULL, TRUE, 'C'),
(29, 82,  NULL, TRUE, 'C'),
(29, 83,  NULL, TRUE, 'C'),
(29, 84,  NULL, TRUE, 'C'),
(29, 85,  NULL, TRUE, 'C'),
(29, 86,  NULL, TRUE, 'C'),
(29, 88,  NULL, TRUE, 'C');

-- Required Core
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(30, 89,  NULL, TRUE, 'C'),  -- FINA 4320
(30, 90,  NULL, TRUE, 'C');  -- FINA 4330

-- Required Track: FINA 4371 Energy Value Chain
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(31, 113, NULL, TRUE, 'C');  -- FINA 4371

-- ECT&C Elective placeholders (choose 5 from approved list)
-- Approved list includes: FINA 4359, 4370, 4372, 4373, 4374, 4376, 4397 (energy topics)
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(32, 133, NULL, TRUE, 'C'),  -- ECTC-ELEC-1
(32, 134, NULL, TRUE, 'C'),  -- ECTC-ELEC-2
(32, 135, NULL, TRUE, 'C'),  -- ECTC-ELEC-3
(32, 136, NULL, TRUE, 'C'),  -- ECTC-ELEC-4
(32, 137, NULL, TRUE, 'C');  -- ECTC-ELEC-5

-- Experiential Learning: FINA 4396 OR FINA 4398 (internship or research project)
INSERT INTO course_set (course_set_id, name) VALUES
(4, 'ECT&C Experiential: Finance Internship OR Research Project');
INSERT INTO course_set_course (course_set_id, course_id) VALUES
(4, 126),  -- FINA 4396
(4, 142);  -- FINA 4398

INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(33, NULL, 4, TRUE, 'C');

-- ECT&C elective placeholders prereq → FINA 3332
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(161, 'COURSE', 83, NULL, 0),
(162, 'COURSE', 83, NULL, 0),
(163, 'COURSE', 83, NULL, 0),
(164, 'COURSE', 83, NULL, 0),
(165, 'COURSE', 83, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES
(72, 133, 'PREREQ', 161),
(73, 134, 'PREREQ', 162),
(74, 135, 'PREREQ', 163),
(75, 136, 'PREREQ', 164),
(76, 137, 'PREREQ', 165);
UPDATE requisite_node SET rule_id = 72 WHERE node_id = 161;
UPDATE requisite_node SET rule_id = 73 WHERE node_id = 162;
UPDATE requisite_node SET rule_id = 74 WHERE node_id = 163;
UPDATE requisite_node SET rule_id = 75 WHERE node_id = 164;
UPDATE requisite_node SET rule_id = 76 WHERE node_id = 165;

SET FOREIGN_KEY_CHECKS = 1;