---
name: adding-a-degree-plan
description: Step-by-step guide for adding a new degree program, track, minor, or courses/prerequisites to CoogPath. Use when writing SQL that inserts into course, degree_program, requirement_group, requirement_item, course_set, requisite_rule, or requisite_node, or when wiring a new program option into ProgramRules and the UI.
---

# Adding or changing a degree plan

Read `flyway-migration-safety` first. Everything here ships as a **new**
`V<n>__*.sql` migration.

## Current ID high-water marks

Highest explicit IDs across migrations V1–V10:

| Column | Max used | Next free |
| --- | --- | --- |
| `course.course_id` | 164 | 165 |
| `degree_program.program_id` | 2 | 3 |
| `requirement_group.group_id` | 31 | 32 |
| `course_set.course_set_id` | 2 | 3 (verify; a dev DB has a stray 3, so 4 is safer) |
| `requisite_node.node_id` | 171 | 172 |
| `requisite_rule.rule_id` | 72 | 73 |

After you add a migration, update this table in the same PR.

Programs: `1` = BS Computer Science, `2` = BBA Finance (both catalog year
2024, 120 credits).

## Data model in one paragraph

A `degree_program` has many `requirement_group`s. Each group has
`requirement_item`s. An item points at either a single `course` or a
`course_set` (any one course in the set satisfies it; the planner schedules the
first by `course_set_course.id`). Prerequisites are trees: a `requisite_rule`
(one per course, `type = 'PREREQ'`) points at a root `requisite_node`; nodes are
`AND`, `OR`, `COURSE` (leaf with `course_id`), or `CONDITION` (not evaluated
yet; always passes).

## Steps

### 1. Courses

```sql
INSERT INTO course (course_id, subject, number, title, credits) VALUES
(165, 'ECON', '3332', 'Intermediate Macroeconomics', 3);
```

- `(subject, number)` is unique. Check that the course doesn't already exist;
  many CS, Finance and core courses do.
- Placeholders ("any approved course") use a non-numeric `number`, e.g.
  `'4XXX-ELEC-1'`, `'LPC-3HR'`. The UI labels any non-four-digit number as
  "Any approved course".
- Subject `ELEC` is reserved for free or general elective placeholders. The
  planner generates free elective slots itself (negative IDs) to reach
  `total_credits_required`, so don't seed "Free Elective" groups for new
  programs; groups whose name contains `Free Elective` are skipped by the planner.

### 2. Program

```sql
INSERT INTO degree_program (program_id, name, college, catalog_year_start, catalog_year_end, total_credits_required)
VALUES (3, 'BS Mathematics', 'College of Natural Sciences and Mathematics', 2024, NULL, 120);
```

### 3. Requirement groups and items

```sql
INSERT INTO requirement_group (group_id, program_id, name, rule_type, min_credits, min_courses, parent_group_id) VALUES
(32, 3, 'Math Core', 'ALL_OF', NULL, NULL, NULL);

INSERT INTO requirement_item (group_id, course_id, course_set_id, required, min_grade) VALUES
(32, 15, NULL, TRUE, 'C'),   -- MATH 2413
(32, NULL, 2, TRUE, 'C');    -- MATH 2318 or MATH 3321
```

- In practice only `ALL_OF` is evaluated: the planner and progress page treat
  every item in an applicable group as required. Model "pick N credits" as N
  credits' worth of placeholder courses (see `CS Advanced Electives`).
- Group names are shown to students on the Requirements page. Keep them short
  and human ("Finance Core", not "FIN_CORE_GRP").
- A course should appear in only one applicable group per student, or the
  progress page double-counts it (the planner de-duplicates; requirements
  don't).

### 4. Prerequisites

Every node in a tree **must** have `rule_id` set. `PrerequisiteGraph` only loads
nodes attached to a rule; a child without `rule_id` is invisible, so an `AND`
parent passes vacuously and an `OR` parent always fails.

```sql
-- ECON 3332 requires ECON 2301 AND (MATH 1314 OR MATH 2413)
INSERT INTO requisite_node (node_id, operator, course_id, parent_node_id, sort_order) VALUES
(172, 'AND',    NULL, NULL, 0),
(173, 'COURSE', 85,   172,  0),   -- ECON 2301
(174, 'OR',     NULL, 172,  1),
(175, 'COURSE', 87,   174,  0),   -- MATH 1314
(176, 'COURSE', 15,   174,  1);   -- MATH 2413
INSERT INTO requisite_rule (rule_id, course_id, type, root_node_id) VALUES
(73, 165, 'PREREQ', 172);
UPDATE requisite_node SET rule_id = 73 WHERE node_id BETWEEN 172 AND 176;
```

- One rule per course. The graph keeps the first rule it sees, so a second
  rule for the same course is ignored. To change a prerequisite, update the
  existing nodes (see V9 repointing node 41).
- Only reference prerequisites the student's plan will actually contain, or
  which they can mark complete. A prerequisite outside the plan creates a dead
  chain and the course shows up under "couldn't be scheduled" (the V9 bug).
- Avoid cycles; the planner will never unlock either course.

### 5. Options (tracks, capstones, minors)

Optional groups are selected **by name** in
`api/.../service/ProgramRules.java#appliesTo`:

- CS: `capstone_choice` ∈ `SENIOR_SE | SENIOR_DS | MATH_MINOR`, matched against
  names containing `Software Engineering`, `Data Science`, and the exact name
  `Math Minor`.
- Finance: `finance_track` maps to a display name (`FINANCE_TRACK_NAMES`) and
  matches `Finance Track: <name>` and `Finance Bauer Electives: <name>`;
  `math_minor` toggles `Finance Math Minor`. With the minor on, ELEC
  placeholders in the Bauer Electives group are covered by the minor
  (`ProgramRules.countsSeparately`).

For a new program with options:
1. Add constants and an `isX(student)` helper to `ProgramRules`; extend
   `appliesTo` and, if the program has a core subject to spread across terms,
   `coreSubject`.
2. If a new student column is needed, add it with a default in the migration,
   on `model/Student.java`, `StudentProfile`, `StudentPreferencesRequest`, and
   validate it in `StudentService.updatePreferences`.
3. Add unit tests to `ProgramRulesTest`.
4. Frontend: add the program ID and option list to `coogpath/src/lib/programs.ts`,
   extend the `StudentProfile` type, and render the options in
   `coogpath/src/pages/SetupPage.tsx` (reuse `OptionGroup`).
5. Update the "Supported programs" section on `LandingPage.tsx`, the README,
   and this skill's ID table.

### 6. Verify

- Fresh database migration and `./mvnw test` (see `flyway-migration-safety`).
- Register a student in the new program locally, mark a few courses, and
  check `/api/plan/generate/{id}` has no unexpected blockers and that
  `/api/requirements/{id}` totals `total_credits_required` (give or take the
  generated free electives).
- Spot-check a few prerequisite chains against the official UH catalog. Don't
  invent course numbers or titles; if unsure, use a placeholder and say so in
  the PR.
