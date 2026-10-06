# CoogPath API

Spring Boot 4 (Java 21) REST API backed by MySQL 8 and Flyway. It serves the
course catalog, stores which courses each student has completed, reports
requirement progress, and generates term-by-term degree plans.

- Base path: `/api`
- Content type: JSON in and out
- Local URL: `http://localhost:8080/api`

## Contents

- [Running locally](#running-locally)
- [Configuration](#configuration)
- [Code layout](#code-layout)
- [Errors](#errors)
- [Endpoints](#endpoints)
- [Testing](#testing)
- [Deployment](#deployment)
- [Security notes](#security-notes)

## Running locally

Requirements: JDK 21 and MySQL 8 running locally.

```bash
mysql -u root -e "CREATE DATABASE coogpath"
cp .env.example .env        # fill in DB_PASSWORD
./mvnw spring-boot:run      # Flyway applies V1..Vn on startup
```

`application.properties` imports `api/.env` when it exists, so the same keys
can be set as environment variables instead.

## Configuration

| Variable | Default (local) | Purpose |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/coogpath` | JDBC URL |
| `DB_USERNAME` | `root` | Database user |
| `DB_PASSWORD` | empty | Database password |
| `CORS_ALLOWED_ORIGINS` | `*` | Comma-separated origin patterns allowed to call `/api/**` |
| `SHOW_SQL` | `false` | Log Hibernate SQL (local profile only) |
| `SERVER_PORT` | `8080` | HTTP port |

Profiles:

- **default**: local development (`application.properties`).
- **prod**: `application-prod.properties`, activated by the Docker
  ENTRYPOINT. Every datasource value must come from the environment, and
  `spring.flyway.clean-disabled=true`.

Schema ownership: Flyway (`src/main/resources/db/migration`). Hibernate runs
with `ddl-auto=none`. See `.agents/skills/flyway-migration-safety` before
adding a migration.

## Code layout

```
src/main/java/com/coogpath/coogpath/
  controller/   HTTP layer only: AuthController, StudentController, PlanController,
                RequirementController, CourseController, DegreeProgramController, HealthController
  service/      StudentService, PlanGeneratorService, RequirementService, CourseService,
                ProgramRules (program-specific group selection), PrerequisiteGraph
  dto/          Request/response types (mostly records)
  model/        JPA entities mapped to the Flyway schema
  repository/   Spring Data JPA repositories
  exception/    ResourceNotFoundException + GlobalExceptionHandler
  config/       SecurityConfig (BCrypt, CORS, permit-all filter chain)
src/main/resources/
  application.properties, application-prod.properties
  db/migration/V*.sql
```

## Errors

Validation and lookup failures return a JSON body:

```json
{ "error": "Use a UH email address (@uh.edu or @cougarnet.uh.edu)." }
```

| Status | When |
| --- | --- |
| `400` | Invalid input (`IllegalArgumentException`) or malformed JSON |
| `401` | Login failed (body is the plain string `Invalid email or password`) |
| `404` | Unknown student (`ResourceNotFoundException`) or unknown route |

## Endpoints

### Health

#### `GET /api/health`

Liveness check; doesn't touch the database.

```json
{ "status": "ok" }
```

### Auth

Login and registration return an [AuthResponse](#authresponse). Every other
`/students/{studentId}`, `/plan/*` and `/requirements/{studentId}` route needs
`Authorization: Bearer <token>`, and the token's subject must equal
`studentId`. Missing, expired or tampered tokens get `401`; another student's
ID gets `403`. Both return `{ "error": "..." }`. `GET /health`, `/programs`
and `/courses` are public.

#### `POST /api/auth/login`

```json
{ "email": "jane@cougarnet.uh.edu", "password": "correct horse" }
```

`200` returns an [AuthResponse](#authresponse). `401` returns
`Invalid email or password` for an unknown email and for a wrong password alike.

### Students

#### `POST /api/students/register`

```json
{
  "name": "Jane Coog",
  "email": "jane@cougarnet.uh.edu",
  "password": "at-least-8-chars",
  "programId": 1,
  "catalogYear": 2024
}
```

Optional fields: `includeSummer` (boolean), `capstoneChoice`.

Input is trimmed and validated:

- all of name, email, password and programId are required;
- the email must end in `@uh.edu` or `@cougarnet.uh.edu`;
- the password must be at least 8 characters;
- the email must not already be registered.

`201` returns an [AuthResponse](#authresponse), so the new student is signed in. `400` returns one of:

- `Name, email, password, and major are required.`
- `Use a UH email address (@uh.edu or @cougarnet.uh.edu).`
- `Password must be at least 8 characters.`
- `Email is already in use.`
- `Degree program not found`

#### `GET /api/students/{studentId}`

`200` returns a [StudentProfile](#studentprofile). `404` if the student doesn't exist.

#### `PATCH /api/students/{studentId}/preferences`

Partial update. Omitted or `null` fields are left unchanged.

```json
{
  "capstoneChoice": "SENIOR_DS",
  "financeTrack": "RE",
  "mathMinor": true,
  "freeElectiveCredits": 6,
  "includeSummer": true
}
```

| Field | Allowed values |
| --- | --- |
| `capstoneChoice` | `SENIOR_SE`, `SENIOR_DS`, `MATH_MINOR` (CS) |
| `financeTrack` | `STANDARD`, `RE`, `PFP`, `CBC`, `GEM`, `ECTC` (Finance) |
| `mathMinor` | boolean (Finance add-on) |
| `freeElectiveCredits` | 0–60; transfer or AP credit that counts toward free electives |
| `includeSummer` | boolean |

`200` returns the updated [StudentProfile](#studentprofile). `400` for an
invalid value.

#### `GET /api/students/{studentId}/courses`

IDs of completed courses (status `TAKEN` or `TRANSFER`).

```json
[1, 15, 16]
```

#### `PUT /api/students/{studentId}/courses`

Replaces the full completed-course list in one transaction. The body is an
array of course IDs.

```json
[1, 15, 16, 86]
```

```json
{ "saved": 4 }
```

Rows are stored with status `TAKEN` and no grade. `400` if any ID is not a
course.

#### `GET /api/students/{studentId}/transcript`

```json
[
  {
    "courseId": 1,
    "courseCode": "COSC 1336",
    "title": "Computer Science and Programming",
    "credits": 3,
    "status": "TAKEN",
    "grade": null
  }
]
```

`status` is one of `TAKEN`, `IN_PROGRESS`, `PLANNED`, `TRANSFER`.

### Catalog

#### `GET /api/courses`

Every course, including placeholder rows such as `COSC 4XXX-ELEC-1`
("any approved course") and `ELEC` free-elective rows.

```json
[{ "courseId": 1, "subject": "COSC", "number": "1336", "title": "Computer Science and Programming", "credits": 3 }]
```

#### `GET /api/programs`

```json
[
  {
    "programId": 1,
    "name": "BS Computer Science",
    "college": "College of Natural Sciences and Mathematics",
    "catalogYearStart": 2024,
    "catalogYearEnd": null,
    "totalCreditsRequired": 120
  }
]
```

### Requirements

#### `GET /api/requirements/{studentId}`

Progress for each requirement group that applies to the student's program and
options (capstone, track, minor). A course-set item appears once, as
`"A or B"` with the title `Choose one`.

```json
[
  {
    "name": "CS Core",
    "totalCredits": 30,
    "completedCredits": 3,
    "courses": [
      { "courseCode": "COSC 1336", "title": "Computer Science and Programming", "credits": 3, "completed": true },
      { "courseCode": "COSC 4351 or COSC 4353", "title": "Choose one", "credits": 3, "completed": false }
    ]
  }
]
```

### Plans

#### `GET /api/plan/generate/{studentId}`

| Query param | Default | Notes |
| --- | --- | --- |
| `mode` | `fastest` | `fastest` (18 credits per fall/spring) or `balanced` (16, and at most 3 COSC/MATH courses per term for CS) |
| `startSeason` | upcoming term | `FALL`, `SPRING`, `SUMMER` |
| `startYear` | upcoming term | e.g. `2027` |
| `includeSummer` | student preference | summers are capped at 6 credits |

```json
{
  "terms": [
    {
      "termLabel": "FALL 2027",
      "season": "FALL",
      "year": 2027,
      "totalCredits": 17,
      "courses": [
        {
          "courseCode": "COSC 1437",
          "title": "Introduction to Programming",
          "credits": 4,
          "prereqString": "Prerequisites Met",
          "reason": "Scheduled by CoogPath Algorithm"
        }
      ]
    }
  ],
  "unmetRequirements": [],
  "blockers": []
}
```

`blockers` lists courses that couldn't be scheduled (for example, a
prerequisite chain that never resolves). The algorithm is described in
`.agents/skills/planner-algorithm`.

#### `POST /api/plan/save/{studentId}`

Stores a generated plan (body: the `PlanResult` above) as a roadmap snapshot.
Returns `201` with no body. The current frontend doesn't call it.

### Shared shapes

#### AuthResponse

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9…", "expiresAt": "2026-10-13T23:00:00Z", "student": { "studentId": 1, "...": "StudentProfile" } }
```

### StudentProfile

Returned by login, register, profile and preference updates. It never
includes the password hash.

```json
{
  "studentId": 17,
  "name": "Jane Coog",
  "email": "jane@cougarnet.uh.edu",
  "programId": 1,
  "programName": "BS Computer Science",
  "capstoneChoice": "SENIOR_SE",
  "financeTrack": "STANDARD",
  "mathMinor": false,
  "freeElectiveCredits": 0,
  "includeSummer": false
}
```

## Testing

```bash
./mvnw test
```

- Unit tests: `ProgramRulesTest`, `PrerequisiteGraphTest`, `StudentServiceTest`,
  `AuthControllerTest`.
- `CoogpathApplicationTests.contextLoads` starts the full context against the
  configured database, which runs every Flyway migration. CI runs this
  against an empty MySQL 8.4 container.

## Deployment

The `Dockerfile` builds with Maven on Temurin 21 and runs the jar on a JRE 21
image:

```
java -XX:MaxRAMPercentage=75 -Dspring.profiles.active=prod -jar app.jar
```

Railway settings:

- Root directory: `api`
- Variables: `DB_URL` (`jdbc:mysql://<host>:<port>/<db>`), `DB_USERNAME`,
  `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS` (e.g. `https://coogpath.vercel.app`),
  `JWT_SECRET` (random, 32+ characters: `openssl rand -base64 48`)
- Optional: `CACHE_TYPE=redis` plus `REDIS_URL` to share cached plans across
  instances and restarts; `JWT_TTL` (default `7d`)
- Health check path: `/api/health`

New migrations run automatically when the service starts. Back up the database
before deploying a migration that changes existing rows.

## Security notes

- Passwords are hashed with BCrypt.
- Sessions are stateless HS256 JWTs signed with `JWT_SECRET` (the API refuses
  to start if it is shorter than 32 bytes; if unset, a random key is used and
  every sign-in ends on restart). `@OwnStudentOnly` limits each student route
  to the token's student. There is no server-side logout; tokens last `JWT_TTL`.
- Requests are rate limited per client IP: 10/min for login and registration,
  30/min for plan generation, 300/min otherwise. Over the limit returns `429`
  with `Retry-After`. Limits are per API instance.
- Generated plans are cached (Caffeine in-process, or Redis with
  `CACHE_TYPE=redis`) under a hash of the catalog version, the student's
  options and completed courses, and the request, so they never go stale.
  Requirements and prerequisites are loaded once per process.
- CORS is restricted by `CORS_ALLOWED_ORIGINS`. Set it in production; the
  default `*` is for local development.
