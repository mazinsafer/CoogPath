-- =======================================================
-- V10: Finance — Commercial Banking and Credit (CBC) Track
-- Required: FINA 4320, 4330, 4341, 4342 (Spring only), 4343, 4380
--           ACCT 3366, 3367, 3368
-- =======================================================

SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO requirement_group (group_id, program_id, name, rule_type, min_credits, min_courses, parent_group_id)
VALUES
(25, 2, 'CBC: Business Foundation',         'ALL_OF', NULL, NULL, NULL),
(26, 2, 'CBC: Required Core',               'ALL_OF', NULL, NULL, NULL),
(27, 2, 'CBC: Commercial Banking Courses',  'ALL_OF', NULL, NULL, NULL),
(28, 2, 'CBC: Advanced Business Courses',   'ALL_OF', NULL, NULL, NULL);

-- Business Foundation
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(25, 81,  NULL, TRUE, 'C'),
(25, 82,  NULL, TRUE, 'C'),
(25, 83,  NULL, TRUE, 'C'),
(25, 84,  NULL, TRUE, 'C'),
(25, 85,  NULL, TRUE, 'C'),
(25, 86,  NULL, TRUE, 'C'),
(25, 88,  NULL, TRUE, 'C');

-- Required Core
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(26, 89,  NULL, TRUE, 'C'),  -- FINA 4320
(26, 90,  NULL, TRUE, 'C');  -- FINA 4330

-- Commercial Banking required courses
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(27, 103, NULL, TRUE, 'C'),  -- FINA 4341
(27, 104, NULL, TRUE, 'C'),  -- FINA 4342 (Spring only)
(27, 105, NULL, TRUE, 'C'),  -- FINA 4343
(27, 94,  NULL, TRUE, 'C');  -- FINA 4380

-- Advanced Business (Accounting sequence)
INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(28, 91,  NULL, TRUE, 'C'),  -- ACCT 3366
(28, 92,  NULL, TRUE, 'C'),  -- ACCT 3367
(28, 93,  NULL, TRUE, 'C');  -- ACCT 3368

SET FOREIGN_KEY_CHECKS = 1;