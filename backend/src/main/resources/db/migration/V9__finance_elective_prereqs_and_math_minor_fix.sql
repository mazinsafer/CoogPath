-- =======================================================
-- V9: Fix two roadmap bugs
--   1) ADV-FIN-ELEC-1..4 had no prereqs, so the scheduler
--      placed them in Fall 2026 alongside freshman-level
--      courses. Add the standard upper-division FINA chain:
--      ECON 2302  AND  FINA 3332  AND  STAT 3331.
--
--   2) MATH MINOR-4XXX (course 77) originally required
--      MATH 3339 (CS-only stats). Finance + Math minor
--      students never schedule MATH 3339, causing a dead
--      prereq chain and a "bottleneck" warning.
--      Repoint the existing prereq node from course 21
--      (MATH 3339) to course 76 (MATH MINOR-3XXX) so the
--      minor chains naturally: MATH 2414 -> 3XXX -> 4XXX.
--
-- node_id continues from 155
-- rule_id continues from 68
-- =======================================================

SET FOREIGN_KEY_CHECKS = 0;


-- =======================================================
-- 1. ADV-FIN-ELEC prereqs
--    Pattern reused from FINA 4357 / 4358 in V5:
--      AND node (root) with 3 COURSE children.
-- =======================================================

-- ---- ADV-FIN-ELEC-1 (course 138) ----
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(156, 'AND',    NULL, NULL, 0),
(157, 'COURSE', 86,   156,  0),   -- ECON 2302
(158, 'COURSE', 83,   156,  1),   -- FINA 3332
(159, 'COURSE', 84,   156,  2);   -- STAT 3331
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES
(69, 138, 'PREREQ', 156);
UPDATE requisite_node SET rule_id = 69 WHERE node_id IN (156, 157, 158, 159);

-- ---- ADV-FIN-ELEC-2 (course 139) ----
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(160, 'AND',    NULL, NULL, 0),
(161, 'COURSE', 86,   160,  0),
(162, 'COURSE', 83,   160,  1),
(163, 'COURSE', 84,   160,  2);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES
(70, 139, 'PREREQ', 160);
UPDATE requisite_node SET rule_id = 70 WHERE node_id IN (160, 161, 162, 163);

-- ---- ADV-FIN-ELEC-3 (course 140) ----
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(164, 'AND',    NULL, NULL, 0),
(165, 'COURSE', 86,   164,  0),
(166, 'COURSE', 83,   164,  1),
(167, 'COURSE', 84,   164,  2);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES
(71, 140, 'PREREQ', 164);
UPDATE requisite_node SET rule_id = 71 WHERE node_id IN (164, 165, 166, 167);

-- ---- ADV-FIN-ELEC-4 (course 141) ----
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(168, 'AND',    NULL, NULL, 0),
(169, 'COURSE', 86,   168,  0),
(170, 'COURSE', 83,   168,  1),
(171, 'COURSE', 84,   168,  2);
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES
(72, 141, 'PREREQ', 168);
UPDATE requisite_node SET rule_id = 72 WHERE node_id IN (168, 169, 170, 171);


-- =======================================================
-- 2. MATH MINOR-4XXX bottleneck fix
--    V4 set node 41 (rule 28, course 77) -> course 21 (MATH 3339).
--    Repoint it to course 76 (MATH MINOR-3XXX) so the chain
--    becomes:  MATH 2414 -> MINOR-3XXX -> MINOR-4XXX.
-- =======================================================
UPDATE requisite_node SET course_id = 76 WHERE node_id = 41;


SET FOREIGN_KEY_CHECKS = 1;
