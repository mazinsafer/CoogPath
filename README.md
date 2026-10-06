# CoogPath

[![CI](https://github.com/mazinsafer/CoogPath/actions/workflows/ci.yml/badge.svg)](https://github.com/mazinsafer/CoogPath/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-8-646CFF?logo=vite&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-7-3178C6?logo=typescript&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)

Semester-by-semester degree planning for University of Houston students.
Mark the courses you've finished, choose your degree options, and CoogPath
schedules the rest of your degree in an order your prerequisites allow.

## Features

- **Term-by-term roadmap** in two modes: *Fastest* (up to 18 credits per
  fall/spring) and *Balanced* (up to 16, with fewer heavy STEM courses per
  term). Summers are optional and capped at 6 credits.
- **Prerequisite-aware scheduling**, including either-or prerequisites and
  "choose one" requirements.
- **Degree options:** CS senior sequence (Software Engineering, Data Science,
  or Math minor); Finance track (Standard, Real Estate, Personal Financial
  Planning, Corporate Banking and Credit, Global Energy Management, Energy
  Commodities Trading and Consulting) with an optional math minor.
- **Transfer and AP credit** toward free electives.
- **Requirement progress** for every group in the degree.
- **Course catalog and transcript** views.
- **PDF export** of the roadmap for advising appointments.

Supported programs (2024 catalog): **BS Computer Science** and **BBA Finance**.

> CoogPath is a student project and is not affiliated with the University of
> Houston. It doesn't check which terms a course is offered; review your plan
> with an advisor.

## Stack

| Layer | Technology |
| --- | --- |
| Web app (`coogpath/`) | React 19, Vite 8, TypeScript, react-router 7, Tailwind CSS 4, oxlint, jsPDF |
| API (`api/`) | Java 21, Spring Boot 4, Spring Data JPA, Spring Security (BCrypt), Lombok |
| Database | MySQL 8, schema and seed data managed by Flyway |
| CI | GitHub Actions: API tests and migrations against a fresh MySQL; web lint and build |
| Hosting | Vercel (web), Railway (API in Docker, MySQL) |

## Repository layout

```
.
├── api/                    Spring Boot REST API
│   ├── src/main/java/com/coogpath/coogpath/
│   │   ├── controller/     HTTP endpoints
│   │   ├── service/        Planner, requirements, students, ProgramRules
│   │   ├── dto/  model/  repository/  exception/  config/
│   ├── src/main/resources/db/migration/   Flyway V1..V10
│   ├── documentation/api.md               Endpoint reference
│   └── Dockerfile
├── coogpath/               React single-page app
│   ├── src/{pages,components,hooks,services,context,lib,types,styles}
│   └── documentation/frontend.md          Frontend guide
├── .agents/skills/         Context for coding agents (architecture, migrations, planner, ...)
├── .github/workflows/ci.yml
└── package.json            Root scripts to run both apps together
```

## Getting started

Prerequisites: **JDK 21**, **MySQL 8**, **Node.js 22** (or 20.19+).

```bash
# 1. Database
mysql -u root -e "CREATE DATABASE coogpath"

# 2. API config
cp api/.env.example api/.env      # set DB_PASSWORD

# 3. Install and run both apps
npm run install:all
npm run dev                       # API → http://localhost:8080, web → http://localhost:5173
```

Flyway creates the schema and seeds the degree data the first time the API
starts. In development the web app proxies `/api` to the API, so it needs no
environment variables.

Run them separately with `npm run dev:api` and `npm run dev:web`.

### Checks

```bash
npm run test:api     # ./mvnw test (needs the local MySQL)
npm run lint:web
npm run build:web
```

## How the planner works

1. Collect every requirement in the student's program that applies to their
   options, minus completed courses, and add free-elective slots up to 120 credits.
2. Each term, find courses whose prerequisite trees are satisfied and rank them
   by how many remaining courses they unlock.
3. Fill the term up to the mode's credit cap, with at least one core-subject
   course (COSC or FINA) per fall/spring term. Balanced CS terms take at most three
   COSC/MATH courses.
4. Merge a short final term into the previous one when it fits under the cap.

Details: [`.agents/skills/planner-algorithm/SKILL.md`](.agents/skills/planner-algorithm/SKILL.md).

## Adding a major

Degree data lives in Flyway migrations. Adding a program means a new
`V<n>__*.sql` with courses, requirement groups and items, and prerequisite
trees. If the program has options such as tracks or minors, it also needs a
small change in `ProgramRules.java` and the setup page. See
[`.agents/skills/adding-a-degree-plan`](.agents/skills/adding-a-degree-plan/SKILL.md)
and [`.agents/skills/flyway-migration-safety`](.agents/skills/flyway-migration-safety/SKILL.md).

## Deployment

| | Railway (API) | Vercel (web) |
| --- | --- | --- |
| Root directory | `api` | `coogpath` |
| Build | `Dockerfile` (runs with the `prod` profile) | Vite preset, `npm run build`, output `dist` |
| Environment | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS` | `VITE_API_URL` (API origin) |

New migrations apply automatically when the API starts. Back up the
production database before deploying a migration that changes existing rows.

## Documentation

- [API reference](api/documentation/api.md)
- [Frontend guide](coogpath/documentation/frontend.md)
- [Agent skills](.agents/skills/)

## Known limitations

- No session or token auth yet. The browser stores the student ID, and API
  routes aren't scoped to the signed-in user.
- Course offerings by term, corequisites, and standing requirements (such as
  junior standing) aren't modeled.
