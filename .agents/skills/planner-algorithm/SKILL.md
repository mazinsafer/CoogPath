---
name: planner-algorithm
description: How CoogPath's term scheduler (PlanGeneratorService) works and how to change it safely. Use when touching plan generation, credit caps, plan modes, summer handling, free electives, or anything that changes which courses land in which term.
---

# Planner algorithm

`api/.../service/PlanGeneratorService.java#generatePlan` is a greedy,
prerequisite-aware scheduler. It is deterministic: the same inputs always
produce the same plan, which lets you diff output before and after a change.

## Inputs

`GET /api/plan/generate/{studentId}?mode=&startSeason=&startYear=&includeSummer=`

- `mode`: `balanced`; anything else means fastest.
- `startSeason`/`startYear`: defaults to the upcoming term (Jan–Aug → Fall of
  this year; Sep–Dec → Spring of next year).
- `includeSummer`: falls back to the student's saved preference.

## Steps

1. **Remaining courses.** For each requirement group of the student's program
   where `ProgramRules.appliesTo` is true (groups named `*Free Elective*` are
   skipped), add every item not yet completed:
   - single course → that course, unless `ProgramRules.countsSeparately` drops it;
   - course set → the first course in the set, unless any option is completed.

   De-duplicate by course ID.
2. **Free electives.** Add `ELEC FREE-<n>HR-<k>` slots (3 credits max each,
   negative IDs) until `completed + freeElectiveCredits + remaining` reaches
   `total_credits_required`.
3. **Prerequisites.** Build a `PrerequisiteGraph` once from all rules and nodes.
   A course is eligible when its tree is satisfied by completed courses plus
   everything scheduled in earlier terms.
4. **Term loop** (max `MAX_TERMS = 16`, skipping summers if disabled):
   - Score each eligible course: `1 + 10 ×` (remaining courses that list it as a
     prerequisite). Higher scores go first, so bottleneck courses are scheduled early.
   - Summer: cap `SUMMER_TERM_CREDITS = 6`; prefers gen-eds, then lower-division
     MATH, then upper-division MATH, then COSC.
   - Fastest: cap `FASTEST_TERM_CREDITS = 18`; at least one course in the
     program's core subject (`COSC` or `FINA`) if any is eligible.
   - Balanced CS: cap `BALANCED_TERM_CREDITS = 16`, at most
     `BALANCED_STEM_CAP = 3` COSC/MATH courses, at least one COSC.
   - Balanced non-CS: cap 16, at least one core-subject course.
   - If nothing fits but something is eligible, schedule the top course anyway.
   - Stop when nothing is eligible (a dead prerequisite chain).
5. **Consolidation.** If the last fall/spring term has 1–2 courses, merge them
   into the previous fall/spring term when they don't depend on it and the
   merged term stays within the mode's cap (18 or 16). Otherwise move
   independent low-credit courses forward to even out the two terms.
6. **Major-course spread.** Move independent COSC/FINA courses from earlier
   fall/spring terms into later ones with fewer than 1–2 major courses. Swap
   later general education courses forward when prerequisites and credit caps
   allow. A term can still have no FINA when finance prerequisites have not
   been completed or there are fewer FINA courses than terms.
7. **Leftovers** become `unmetRequirements` and `blockers` (shown as a warning
   on the roadmap).

## Changing it

- Keep the constants at the top of the class, and update this skill and the
  roadmap mode hint in `coogpath/src/pages/RoadmapPage.tsx` (`MODE_HINTS`) if
  caps change.
- Program-specific behaviour belongs in `ProgramRules`, not in `if (programId == 2)`
  checks inside the loop.
- Anything that changes which items count must also change
  `RequirementService`, or the Requirements page and Roadmap will disagree.
- Before and after any change, capture plans for a matrix of students (CS with
  each capstone; Finance with STANDARD, RE, and ECTC, each with math minor on
  and off; fastest and balanced; summers on and off) and diff them. Explain
  every difference in the PR.
- `generatePlan` is `@Transactional(readOnly = true)`. Don't write from it;
  saving a plan is the separate `POST /api/plan/save/{studentId}`.

## Known limitations

- No term availability (every course is assumed offered every term).
- `CONDITION` nodes (e.g. junior standing) always pass.
- Corequisites are not modeled.
- Course sets always schedule their first option.
