-- =======================================================
-- V9: Finance — Program for Financial Planning (PFP) Track
-- Required: STAT 3331, FINA 4320, 4325, 4330, 4352, 4353, 4354
--           ACCT 4331
-- =======================================================

SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO requirement_group (group_id, program_id, name, rule_type, min_credits, min_courses, parent_group_id)
VALUES
(23, 2, 'PFP: Business Foundation',   'ALL_OF', NULL, NULL, NULL),
(24, 2, 'PFP: Required Track Courses','ALL_OF', NULL, NULL, NULL);

-- Business Foundation
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(23, 81,  NULL, TRUE, 'C'),
(23, 82,  NULL, TRUE, 'C'),
(23, 83,  NULL, TRUE, 'C'),
(23, 84,  NULL, TRUE, 'C'),  -- STAT 3331
(23, 85,  NULL, TRUE, 'C'),
(23, 86,  NULL, TRUE, 'C'),
(23, 88,  NULL, TRUE, 'C');

-- Required Track Courses
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(24, 89,  NULL, TRUE, 'C'),  -- FINA 4320
(24, 90,  NULL, TRUE, 'C'),  -- FINA 4330
(24, 98,  NULL, TRUE, 'C'),  -- FINA 4352
(24, 99,  NULL, TRUE, 'C'),  -- FINA 4325
(24, 100, NULL, TRUE, 'C'),  -- FINA 4353
(24, 101, NULL, TRUE, 'C'),  -- FINA 4354
(24, 102, NULL, TRUE, 'C');  -- ACCT 4331

-- ACCT 4331 prereq → ACCT 2301 AND ACCT 2302
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(157, 'AND',    NULL, NULL, 0),
(158, 'COURSE', 81,   157,  0),
(159, 'COURSE', 82,   157,  1);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES (70, 102, 'PREREQ', 157);
UPDATE requisite_node SET rule_id = 70 WHERE node_id IN (157,158,159);

SET FOREIGN_KEY_CHECKS = 1;