# CoogPath

AI-powered degree planning for University of Houston students. CoogPath helps students build personalized semester-by-semester roadmaps to graduation based on their completed coursework, degree requirements, and scheduling preferences.

## Features

- **Currently in process of adding more majors**

- **Smart Roadmap Generation** — Two planning modes: *Fastest* (graduate in ~3.5 years) and *Balanced* (graduate in ~4 years), with configurable summer semester inclusion
- **Prerequisite-Aware Scheduling** — Greedy scheduling algorithm that respects prerequisite chains, corequisites, and course availability
- **CS Capstone & Minor Support** — Choose between a Software Engineering senior sequence, Data Science senior sequence, or Math Minor
- **Free Elective Tracking** — Enter transfer/AP/dual credit free elective hours to reduce planned elective slots
- **Degree Requirements Dashboard** — Visual progress tracking across all requirement groups
- **Course Catalog & Transcript** — Browse all available courses and view your completed coursework
- **PDF Export** — Download your roadmap as a PDF
- **UH Email Enforcement** — Registration restricted to `@uh.edu` and `@cougarnet.uh.edu` emails
- **Advisor Chatbot Coming Soon**

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | Next.js 16, React 19, TypeScript, Tailwind CSS 4 |
| **Backend** | Spring Boot 4, Spring Data JPA, Spring Security, Lombok |
| **Database** | MySQL |
| **Migrations** | Flyway (local), Hibernate DDL auto-update (production) |
| **PDF Generation** | jsPDF (client-side) |
| **Deployment** | Vercel (frontend), Railway (backend + MySQL) |

## Project Structure

```
coogpath/
├── backend/                        # Spring Boot API
│   ├── src/main/java/com/coogpath/coogpath/
│   │   ├── controller/             # REST endpoints
│   │   ├── model/                  # JPA entities
│   │   ├── repository/             # Spring Data repositories
│   │   ├── service/                # Business logic
│   │   ├── dto/                    # Data transfer objects
│   │   └── config/                 # Security & CORS config
│   ├── src/main/resources/
│   │   ├── db/migration/           # Flyway SQL migrations
│   │   ├── application.properties  # Default config
│   │   └── application-prod.properties
│   ├── Dockerfile                  # Multi-stage Docker build
│   └── pom.xml
├── frontend/                       # Next.js app
│   ├── src/app/
│   │   ├── page.tsx                # Landing page
│   │   ├── signup/                 # Registration
│   │   ├── login/                  # Authentication
│   │   ├── courses/                # Course selection & preferences
│   │   ├── dashboard/              # Roadmap & plan summary
│   │   ├── requirements/           # Degree requirements progress
│   │   ├── catalog/                # Course catalog browser
│   │   └── transcript/             # Student transcript
│   ├── next.config.ts
│   └── package.json
└── package.json                    # Monorepo scripts (concurrently)
```

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Authenticate student |
| POST | `/api/students/register` | Register new student |

### Student
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/students/{id}/courses` | Get completed course IDs |
| POST | `/api/students/{id}/courses` | Sync completed courses |
| GET | `/api/students/{id}/transcript` | Get transcript records |
| PATCH | `/api/students/{id}/capstone` | Update capstone/minor choice |
| PATCH | `/api/students/{id}/free-elective-credits` | Update free elective credits |

### Planning
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/plan/generate/{id}` | Generate degree roadmap |
| GET | `/api/requirements/{id}` | Get requirement progress |

### Reference Data
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/programs` | List all degree programs |
| GET | `/api/courses` | List all courses |

## Getting Started

### Prerequisites

- Java 17+
- Maven
- MySQL 8+
- Node.js 18+
- npm

### Database Setup

Create a local MySQL database:

```sql
CREATE DATABASE coogpath;
```

### Environment Variables

Create `backend/.env`:

```properties
DB_URL=jdbc:mysql://localhost:3306/coogpath
DB_USERNAME=root
DB_PASSWORD=your_password
```

Create `frontend/.env.local`:

```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### Run the Application

From the project root:

```bash
# Install all dependencies
npm run install:all

# Run both backend and frontend concurrently
npm run dev
```

Or run them separately:

```bash
# Backend (port 8080)
cd backend && ./mvnw spring-boot:run

# Frontend (port 3000)
cd frontend && npm run dev
```

The frontend proxies `/api/*` requests to the backend automatically.

## Database Schema

The application uses 14 tables managed through Flyway migrations:

- **`course`** — Course catalog (subject, number, title, credits)
- **`term`** — Academic terms (season, year)
- **`degree_program`** — Degree programs (name, total credits required)
- **`requirement_group`** — Groups of requirements within a degree
- **`requirement_item`** — Individual requirements (specific course or course set)
- **`course_set` / `course_set_course`** — "Choose one from" course groups
- **`requisite_rule` / `requisite_node`** — Prerequisite/corequisite trees
- **`student`** — Student records with preferences
- **`student_course`** — Student's completed/in-progress courses
- **`roadmap_snapshot` / `roadmap_semester` / `roadmap_semester_course`** — Saved roadmap plans

## Adding a New Major

The application is data-driven. To add a new major (e.g., Finance):

1. Create a new SQL migration file (e.g., `V5__finance_degree_plan.sql`)
2. Insert the degree program into `degree_program`
3. Insert all required courses into `course`
4. Create requirement groups and items in `requirement_group` / `requirement_item`
5. Define prerequisite trees in `requisite_rule` / `requisite_node`

No backend or frontend code changes are needed — the UI dynamically adapts to the data.

## Deployment

### Backend (Railway)

The backend deploys via the `backend/Dockerfile` with the `prod` Spring profile:

```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
```

### Frontend (Vercel)

- Set the root directory to `frontend`
- Add environment variable: `NEXT_PUBLIC_API_URL` = your Railway backend URL

## Roadmap Algorithm

The plan generator uses a greedy scheduling approach:

1. **Determine remaining courses** from the student's degree requirements minus completed courses
2. **Filter eligible courses** whose prerequisites are satisfied
3. **Score courses** by how many other courses depend on them (prerequisite depth)
4. **Select courses per semester** based on the chosen mode:
   - *Fastest*: Up to 18 credits/semester, no STEM cap, greedy fill
   - *Balanced*: Up to 16 credits/semester, max 3 CS/MATH courses
5. **Summer semesters** (if enabled): Prioritize non-CS courses, then lower-level math, then CS
6. **Free elective slots** are dynamically generated to fill any credit deficit to 120
7. **Post-processing** consolidates single-course trailing semesters

Both modes guarantee at least 1 CS course per Fall/Spring semester.
