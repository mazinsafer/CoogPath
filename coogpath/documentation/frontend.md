# CoogPath web app

Single-page React app for CoogPath. Students create an account, mark the
courses they've completed, pick degree options, and get a term-by-term
roadmap they can export as a PDF.

| | |
| --- | --- |
| Framework | React 19, react-router-dom 7 |
| Build | Vite 8, TypeScript 7 (strict) |
| Styling | Tailwind CSS 4 via `@tailwindcss/vite`; tokens in `src/styles/index.css` |
| Lint | oxlint (`.oxlintrc.json`) |
| PDF | jsPDF, loaded on demand when exporting |
| Hosting | Vercel (static build, SPA rewrite in `vercel.json`) |

## Running locally

```bash
npm install
npm run dev        # http://localhost:5173
```

Start the API too (`cd ../api && ./mvnw spring-boot:run`, or `npm run dev`
from the repo root). In development `/api/*` is proxied to
`http://localhost:8080` (`vite.config.ts`), so no env var is needed.

Scripts:

| Script | What it does |
| --- | --- |
| `npm run dev` | Vite dev server |
| `npm run build` | Type-check (`tsc -b`) and build to `dist/` |
| `npm run typecheck` | Type-check only |
| `npm run lint` | oxlint |
| `npm run preview` | Serve the production build |

## Environment

| Variable | Example | Notes |
| --- | --- | --- |
| `VITE_API_URL` | `https://coogpath-api.up.railway.app` | API origin. Leave unset locally. `/api` is added to each request; a trailing `/api` is ignored. |

Vite inlines `VITE_*` variables at build time, so changing one in Vercel
requires a redeploy.

## Folder structure

```
src/
  main.tsx              Entry: BrowserRouter > SessionProvider > App
  App.tsx               Route table
  pages/                One component per route
  components/
    ui/                 Primitives: Button, ButtonLink, Card, Field, Toggle, SegmentedControl,
                        Alert, Badge, ProgressBar, StatGrid, States (Loading/Empty/Error), Spinner
    layout/             AppLayout, Sidebar, AuthLayout, PageHeader, RequireAuth, Logo, SiteFooter
    roadmap/TermCard    One planned term
    courses/CourseFilters  Search box + subject chips
    setup/OptionGroup   Radio cards for capstone / track options
    TermSelect          Start-term picker
    Icons.tsx           Inline outline icons
  hooks/
    useAsync            Runs a request, keeps previous data while reloading, ignores stale responses
    useStudentData      One hook per API resource (profile, plan, requirements, transcript, courses, programs)
    useCourseFilter     Search + subject filter + grouping for course lists
    useSession          Session context accessors
    useDocumentTitle    Sets "<Page> · CoogPath"
  services/
    api.ts              fetch wrapper: base URL, JSON, ApiError with status + server message
    authService, studentService, catalogService, planService   Typed endpoint functions
    sessionStore        localStorage persistence for the session and chosen start term
    pdfExport           Roadmap PDF
  context/              SessionProvider + context object
  lib/                  Pure helpers: terms (season/year math), programs (IDs, options), format
  types/                TypeScript mirrors of the API DTOs
  styles/index.css      Tailwind import, theme tokens, base styles
```

## Routes

| Path | Page | Auth |
| --- | --- | --- |
| `/` | `LandingPage` | public |
| `/login` | `LoginPage` (redirects back to the page that sent you) | public |
| `/signup` | `SignupPage` → `/setup` on success | public |
| `/roadmap` | `RoadmapPage` | required |
| `/requirements` | `RequirementsPage` | required |
| `/transcript` | `TranscriptPage` | required |
| `/catalog` | `CatalogPage` | required |
| `/setup` | `SetupPage` (completed courses + degree options) | required |
| `/dashboard`, `/courses` | Redirect to `/roadmap`, `/setup` (old Next.js URLs) | — |
| `*` | `NotFoundPage` | — |

Authenticated routes are nested under `<RequireAuth><AppLayout/></RequireAuth>`,
which redirects to `/login` and renders the sidebar shell.

## Data flow

```
page ──> hook (useStudentData) ──> service ──> api.ts ──> fetch /api/...
```

- Pages don't call `fetch` directly.
- `useAsync` returns `{ data, loading, error, reload }`. While a request is in
  flight the previous `data` stays available, which is how the roadmap
  re-generates without flashing when you change mode or start term.
- `api.ts` throws `ApiError` with the server's `{"error": ...}` message (or a
  plain-text body), so pages can show it as is.

## Session

There is no auth token (see the API docs). After login or signup the app
stores `{ studentId, name }` under `localStorage["coogpath.session"]`. The
chosen start term is stored under `coogpath.startTerm`. All other preferences
(capstone, track, minor, summers, transfer credit) are saved on the server via
`PATCH /students/{id}/preferences`.

On first load, `sessionStore` migrates the keys written by the old Next.js app
(`studentId`, `studentName`, `startSemester`, …), so existing users stay
signed in, and then deletes them.

## Pages

- **Roadmap**: calls `GET /plan/generate/{id}` with the selected mode
  (Fastest/Balanced), start term and summer setting. Changing summers also
  saves the preference. Shows remaining credits, terms, courses and estimated
  graduation, any blockers, and one `TermCard` per term. Export PDF uses
  `pdfExport`.
- **Courses & options** (`/setup`): degree options (CS senior sequence, or
  Finance track plus math minor), first term to plan, summers, transfer/AP
  free-elective credit, and the completed-course checklist. Save sends one
  `PATCH` for preferences and one `PUT /students/{id}/courses` with the full
  course list, then opens the roadmap.
- **Requirements**: overall and per-group progress from `GET /requirements/{id}`.
- **Transcript**: completed courses from `GET /students/{id}/transcript`.
- **Catalog**: searchable list of every course (planner-generated `ELEC` rows hidden).

## Design system

- Colors: zinc neutrals, with `brand-*` (UH red, `brand-600 = #c8102e`) for
  primary actions, focus and progress. Emerald means "complete". Tokens are
  defined in `@theme` in `src/styles/index.css`.
- Type: Inter, `text-sm` body, sentence case everywhere.
- Surfaces: `rounded-lg border border-zinc-200 bg-white`; avoid heavy shadows,
  gradients and decorative effects.
- Reuse the primitives in `components/ui` before writing new markup.

See `.agents/skills/frontend-conventions` for the full set of rules.

## Deployment (Vercel)

- Root directory: `coogpath`
- Framework preset: Vite (build `npm run build`, output `dist`)
- Environment: `VITE_API_URL` = the Railway API origin
- `vercel.json` rewrites every path to `index.html` so deep links like
  `/roadmap` work on refresh.
- The API's `CORS_ALLOWED_ORIGINS` must include the Vercel domain.
