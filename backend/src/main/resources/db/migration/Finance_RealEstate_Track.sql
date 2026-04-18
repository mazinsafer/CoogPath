-- =======================================================
-- V7: Finance — Real Estate Finance Track
-- Required: FINA 4320, 4330, 4380, 4383
-- Choose 1 of: FINA 4382  OR FINA 4397 
-- =======================================================

SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO requirement_group (group_id, program_id, name, rule_type, min_credits, min_courses, parent_group_id)
VALUES
(15, 2, 'RE: Business Foundation',     'ALL_OF',   NULL, NULL, NULL),
(16, 2, 'RE: Required Core',           'ALL_OF',   NULL, NULL, NULL),
(17, 2, 'RE: Required Track Courses',  'ALL_OF',   NULL, NULL, NULL),
(18, 2, 'RE: Capstone Choice',         'CHOOSE_N', NULL, 1,    NULL);

-- Business Foundation
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(15, 81,  NULL, TRUE, 'C'),
(15, 82,  NULL, TRUE, 'C'),
(15, 83,  NULL, TRUE, 'C'),
(15, 84,  NULL, TRUE, 'C'),
(15, 85,  NULL, TRUE, 'C'),
(15, 86,  NULL, TRUE, 'C'),
(15, 88,  NULL, TRUE, 'C');

-- Required Core
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(16, 89,  NULL, TRUE, 'C'),  -- FINA 4320
(16, 90,  NULL, TRUE, 'C');  -- FINA 4330

-- Required Track: 4380 and 4383
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(17, 94,  NULL, TRUE, 'C'),  -- FINA 4380
(17, 95,  NULL, TRUE, 'C');  -- FINA 4383

-- Capstone Choice: 4382 (Fall only) OR 4397 (Spring only)
-- Use course_set_id = 3
INSERT INTO course_set (course_set_id, name) VALUES
(3, 'RE Capstone: Developing a Real Estate Project OR Asset Management');
INSERT INTO course_set_course (course_set_id, course_id) VALUES
(3, 96),   -- FINA 4382 Fall only
(3, 97);   -- FINA 4397 Spring only

INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(18, NULL, 3, TRUE, 'C');

SET FOREIGN_KEY_CHECKS = 1;