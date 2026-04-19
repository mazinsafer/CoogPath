-- =======================================================
-- V8: Two small Finance-plan corrections
--   1. STAT 3331 now requires BUSI 2305 (Business Statistics).
--   2. Rename MATH 1324 to its Bauer-friendly title "Finite Math
--      with Applications".
--
-- node_id continues from 154, rule_id continues from 67.
-- =======================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. MATH 1324 title fix --------------------------------
UPDATE course
SET title = 'Finite Math with Applications'
WHERE course_id = 88;          -- MATH 1324


-- 2. STAT 3331 (course_id 84) prereq -> BUSI 2305 (course_id 146)
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(155, 'COURSE', 146, NULL, 0);

INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES
(68, 84, 'PREREQ', 155);

UPDATE requisite_node SET rule_id = 68 WHERE node_id = 155;

SET FOREIGN_KEY_CHECKS = 1;
