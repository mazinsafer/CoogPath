-- =======================================================
-- V6: Finance — Standard Track
-- The default track: FINA 4320 + 4330 required,
-- then 12 credits of upper-division Finance electives.
-- group_ids continue from 11 → start at 12
-- =======================================================

SET FOREIGN_KEY_CHECKS = 0;

-- Requirement Groups for Standard Track (program_id = 2)
INSERT INTO requirement_group (group_id, program_id, name, rule_type, min_credits, min_courses, parent_group_id)
VALUES
(12, 2, 'Finance: Business Foundation',   'ALL_OF',      NULL, NULL, NULL),
(13, 2, 'Finance: Required Core',         'ALL_OF',      NULL, NULL, NULL),
(14, 2, 'Finance: Upper-Div Electives',   'MIN_CREDITS', 12,   NULL, NULL);

-- Business Foundation items
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(12, 81,  NULL, TRUE, 'C'),  -- ACCT 2301
(12, 82,  NULL, TRUE, 'C'),  -- ACCT 2302
(12, 83,  NULL, TRUE, 'C'),  -- FINA 3332
(12, 84,  NULL, TRUE, 'C'),  -- STAT 3331
(12, 85,  NULL, TRUE, 'C'),  -- ECON 2301
(12, 86,  NULL, TRUE, 'C'),  -- ECON 2302
(12, 88,  NULL, TRUE, 'C');  -- MATH 1324

-- Required Core items
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(13, 89,  NULL, TRUE, 'C'),  -- FINA 4320
(13, 90,  NULL, TRUE, 'C');  -- FINA 4330

-- Upper-div elective placeholders (choose 12 cr from approved Finance 4XXX list)
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(14, 138, NULL, TRUE, 'C'),  -- STD-ELEC-1
(14, 139, NULL, TRUE, 'C'),  -- STD-ELEC-2
(14, 140, NULL, TRUE, 'C'),  -- STD-ELEC-3
(14, 141, NULL, TRUE, 'C');  -- STD-ELEC-4

-- STD elective placeholders → FINA 3332
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(153, 'COURSE', 83, NULL, 0),
(154, 'COURSE', 83, NULL, 0),
(155, 'COURSE', 83, NULL, 0),
(156, 'COURSE', 83, NULL, 0);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES
(66, 138, 'PREREQ', 153),
(67, 139, 'PREREQ', 154),
(68, 140, 'PREREQ', 155),
(69, 141, 'PREREQ', 156);
UPDATE requisite_node SET rule_id = 66 WHERE node_id = 153;
UPDATE requisite_node SET rule_id = 67 WHERE node_id = 154;
UPDATE requisite_node SET rule_id = 68 WHERE node_id = 155;
UPDATE requisite_node SET rule_id = 69 WHERE node_id = 156;

SET FOREIGN_KEY_CHECKS = 1;