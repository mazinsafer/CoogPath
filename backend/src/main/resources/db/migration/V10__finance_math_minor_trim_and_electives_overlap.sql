-- =======================================================
-- V10: Shrink Finance Math Minor to match UH Math Minor
-- ---------------------------------------------------------
-- Per UH: Math Minor (upper division) = 6 hrs of 3000-level
-- MATH  + 3 hrs of 4000-level MATH.  Foundation is Calc I
-- and Calc II.  Total: 17 credits.
--
-- Before V10, group 20 ("Finance Math Minor") had:
--   Calc I, Calc II, MINOR-3XXX, MINOR-4XXX,
--   FIN-MINOR-3XXX, FIN-MINOR-4XXX   (20 credits — too many)
--
-- After V10:
--   Calc I, Calc II, MINOR-3XXX, MINOR-4XXX,
--   FIN-MINOR-3XXX                   (17 credits)
-- i.e. 2 × 3000-level  +  1 × 4000-level  +  Calc I/II.
-- =======================================================

SET FOREIGN_KEY_CHECKS = 0;

-- Drop the duplicate 4000-level placeholder from the minor group.
DELETE FROM requirement_item
WHERE group_id = 20 AND course_id = 143;   -- FIN-MINOR-4XXX

SET FOREIGN_KEY_CHECKS = 1;
