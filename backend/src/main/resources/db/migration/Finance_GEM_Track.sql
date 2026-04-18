-- =======================================================
-- V8: Finance — Global Energy Management (GEM) Track
-- Required: FINA 4320, 4330
-- Select 12 credits from GEM Finance Electives list
-- Select 6 credits from Advanced GEM Electives list
-- =======================================================

SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO requirement_group (group_id, program_id, name, rule_type, min_credits, min_courses, parent_group_id)
VALUES
(19, 2, 'GEM: Business Foundation',      'ALL_OF',      NULL, NULL, NULL),
(20, 2, 'GEM: Required Core',            'ALL_OF',      NULL, NULL, NULL),
(21, 2, 'GEM: Finance Electives',        'MIN_CREDITS', 12,   NULL, NULL),
(22, 2, 'GEM: Advanced Business Electives','MIN_CREDITS',6,   NULL, NULL);

-- Business Foundation
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(19, 81,  NULL, TRUE, 'C'),
(19, 82,  NULL, TRUE, 'C'),
(19, 83,  NULL, TRUE, 'C'),
(19, 84,  NULL, TRUE, 'C'),
(19, 85,  NULL, TRUE, 'C'),
(19, 86,  NULL, TRUE, 'C'),
(19, 88,  NULL, TRUE, 'C');

-- Required Core
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(20, 89,  NULL, TRUE, 'C'),  -- FINA 4320
(20, 90,  NULL, TRUE, 'C');  -- FINA 4330

-- GEM Finance Electives (choose 12 cr from this list)
-- FINA 4350, 4351, 4357, 4358, 4359, 4360, 4370, 4371, 4372, 4373, 4374, 4376, 4397
-- Using placeholders for the 4 slots
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(21, 127, NULL, TRUE, 'C'),  -- GEM-ELEC-1
(21, 128, NULL, TRUE, 'C'),  -- GEM-ELEC-2
(21, 129, NULL, TRUE, 'C'),  -- GEM-ELEC-3
(21, 130, NULL, TRUE, 'C');  -- GEM-ELEC-4

-- GEM Advanced Electives (choose 6 cr from this list)
-- ACCT 3366, SCM 4302, SCM 4311, TECH 4310, ECON 3385, HIST 3349, ENRG 3310, ENRG 4320
-- Using placeholders for the 2 slots
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(22, 131, NULL, TRUE, 'C'),  -- GEM-ADV-1
(22, 132, NULL, TRUE, 'C');  -- GEM-ADV-2

SET FOREIGN_KEY_CHECKS = 1;