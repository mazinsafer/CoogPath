-- =======================================================
-- CoogPath Dummy Student Seed Data
-- Run AFTER cs_degree_plan.sql
-- =======================================================

USE coogpath;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Create our test student: Jorge Coog
INSERT INTO student (
    student_id, name, email, password_hash, program_id, 
    catalog_year, max_credits_fall, max_credits_spring, max_credits_summer, include_summer
) VALUES (
    1, 'Jorge Coog', 'jorge@uh.edu', 'dummy_hash_for_testing', 1, 
    2024, 15, 15, 6, FALSE
);

-- 2. Give Jorge his first semester transcript
-- We will say he took Intro to CS, Calc I, English I, and US History I.
INSERT INTO student_course (student_id, course_id, status, grade, attempt_no) VALUES
(1, 1,  'TAKEN', 'A', 1),   -- COSC 1336 (Computer Science and Programming)
(1, 15, 'TAKEN', 'B', 1),   -- MATH 2413 (Calculus I)
(1, 22, 'TAKEN', 'A', 1),   -- ENGL 1301 (First Year Writing I)
(1, 24, 'TAKEN', 'A', 1);   -- HIST 1301 (American History I)

SET FOREIGN_KEY_CHECKS = 1;