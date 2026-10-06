---
name: coogpath-architecture
description: Orientation for any work in the CoogPath repo. Read first. Covers the folder layout, how the Spring Boot API and the React app fit together, where program-specific rules live, how to run and verify changes, and the production facts an agent must not break.
---

# CoogPath architecture

CoogPath builds semester-by-semester degree plans for University of Houston
students. A student marks completed courses; the API walks the degree's
requirement groups and prerequisite trees and returns a term-by-term schedule.

## Layout

```
api/          Spring Boot 4 (Java 21) REST API, Flyway migrations, MySQL
coogpath/     React 19 + Vite + TypeScript + Tailwind 4 single-page app
.agents/      Agent skills (this folder)
.github/      CI workflow
documentation/ Local-only notes (gitignored), e.g. data-architecture.md
```

Detailed references:
- `api/documentation/api.md`: every endpoint, request/response shapes, config.
- `coogpath/documentation/frontend.md`: routing, data flow, design system.

## Request flow

1. The browser calls `${VITE_API_URL}/api/...` through `coogpath/src/services/api.ts`.
   In local dev `VITE_API_URL` is empty and Vite proxies `/api` to `localhost:8080`.
2. Controllers in `api/.../controller` are thin. Business logic lives in
   `api/.../service`.
3. Services read JPA entities (`model/`) through Spring Data repositories and
   return DTO records (`dto/`). Controllers never return the `Student` entity
   (it holds `passwordHash`); use `StudentProfile.from(student)`.
4. Errors: throw `ResourceNotFoundException` (404) or `IllegalArgumentException`
   (400). `GlobalExceptionHandler` turns them into `{"error": "..."}`.

## Where the rules live

| Concern | File |
| --- | --- |
| Which requirement groups apply (capstone, Finance track, math minor) | `api/.../service/ProgramRules.java` |
| Prerequisite tree evaluation | `api/.../service/PrerequisiteGraph.java` |
| Scheduling | `api/.../service/PlanGeneratorService.java` (see the `planner-algorithm` skill) |
| Requirement progress | `api/.../service/RequirementService.java` |
| Degree data (courses, groups, prereqs) | `api/src/main/resources/db/migration/V*.sql` |
| Program IDs and option labels in the UI | `coogpath/src/lib/programs.ts` |

`ProgramRules` selects groups by `requirement_group.name`. Renaming a group in
SQL without updating `ProgramRules` silently changes plans.

The planner and the requirements page must always agree. Any rule that drops or
adds a requirement item goes through `ProgramRules` and is used by both
`PlanGeneratorService` and `RequirementService`.

## Production facts

- Hosting: API on Railway (Docker, root directory `api`), MySQL on Railway,
  frontend on Vercel (root directory `coogpath`, Vite preset).
- The prod profile is activated by the Dockerfile ENTRYPOINT. Secrets come only
  from environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`,
  `CORS_ALLOWED_ORIGINS`). Never commit credentials; `api/.env` is gitignored.
- Flyway owns the schema. Hibernate runs with `ddl-auto=none`.
- In April 2026 a reset script dropped every production table to let Flyway
  rerun V1, which deleted all registered students. Read the
  `flyway-migration-safety` skill before touching anything schema-related.

## Running and verifying

```bash
npm run install:all     # root + coogpath deps
npm run dev             # API on :8080, web on :5173
cd api && ./mvnw test   # unit tests + context load (needs local MySQL)
cd coogpath && npm run build && npm run lint
```

When you change planner or requirement logic, compare `GET /api/plan/generate/{id}`
and `GET /api/requirements/{id}` before and after for a CS student (each capstone
choice) and a Finance student (several tracks, math minor on and off). Unless the
change is meant to alter plans, the output must be identical.

## Auth

Login and registration return a JWT whose subject is the student ID. Any new
endpoint that takes a student ID must be annotated `@OwnStudentOnly` (and name
the parameter `studentId`), and must not be added to the `permitAll` list in
`SecurityConfig` unless it serves only public catalog data.

## Caching

`CatalogCache` holds requirement groups and the prerequisite graph for the life
of the process, so a migration that changes them takes effect on restart.
`PlanCache` keys plans by every planner input; if you add a new input to plan
generation (a preference, a transcript field), add it to the key in
`PlanGeneratorService.generatePlan` or students will get stale plans.
