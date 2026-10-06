---
name: frontend-conventions
description: Conventions for the CoogPath React app in coogpath/. Use when adding or editing pages, components, hooks, services, styles, or copy in the frontend.
---

# Frontend conventions

Stack: React 19, Vite, TypeScript (strict), react-router-dom 7, Tailwind 4
(`@tailwindcss/vite`, tokens in `src/styles/index.css`), oxlint. No component
library; small primitives live in `src/components/ui`.

## Structure

```
src/
  pages/        One component per route, named exports (LoginPage, RoadmapPage, ...)
  components/
    ui/         Generic primitives: Button, Card, Field, Alert, Badge, Toggle, States, ...
    layout/     AppLayout, Sidebar, AuthLayout, PageHeader, RequireAuth, Logo
    <feature>/  Feature components (roadmap/TermCard, courses/CourseFilters, setup/OptionGroup)
  hooks/        useAsync, useStudentData (one hook per API resource), useCourseFilter, useSession
  services/     api.ts (fetch wrapper) + one module per API area; sessionStore, pdfExport
  context/      SessionProvider and its context object
  lib/          Pure helpers: terms, programs, format
  types/        API response and request types, mirroring the Java DTOs
```

## Rules

- **Data flow:** page → hook in `useStudentData.ts` → service → `api.ts`. Pages
  never call `fetch`. New endpoints get a typed service function and, if read
  by a page, a hook.
- **Types mirror DTOs.** When a Java DTO changes, update `src/types/*` in the
  same PR.
- **States:** every data-backed view handles loading (`LoadingState`), error
  (`ErrorState` with `onRetry`), and empty (`EmptyState`).
- **Forms that depend on loaded data:** load in the page, then render an inner
  form component that initialises `useState` from props (see `SetupPage` →
  `SetupForm`, `RoadmapPage` → `Roadmap`). Don't sync props into state with
  effects.
- **Protected pages** sit under `<RequireAuth>` in `App.tsx` and read the
  student via `useRequiredSession()`.
- **Program-specific UI** keys off `lib/programs.ts` (`isComputerScience`,
  `isFinance`, option lists). Keep IDs in sync with `ProgramRules.java`.
- **Titles:** call `useDocumentTitle("Page name")` in every page.

## Visual style

The goal is a calm, credible tool, not a marketing template.

- Light zinc neutrals; UH red (`brand-600`, `#c8102e`) only for primary actions,
  focus rings, the active state and progress. Emerald only for "complete".
- Type: Inter; `text-sm` body, `text-2xl font-semibold tracking-tight` page
  titles (see `PageHeader`). Use `tabular-nums` for numbers in columns.
- Borders over shadows: `border border-zinc-200 rounded-lg bg-white`. At most
  `shadow-sm` on controls.
- No gradients, glows, glassmorphism, emoji, or decorative icons in headings.
  Icons are 16px outline icons from `components/Icons.tsx`.
- Sentence case for headings and buttons ("Save and view roadmap").

## Copy

- Never invent stats, testimonials, user counts, or accuracy claims.
- Don't claim official UH affiliation; keep the footer disclaimer.
- Describe what the planner does in plain terms, and say what it doesn't do
  (it doesn't check course availability by term).
- Error messages come from the API's `{"error": ...}`; keep backend messages
  user-readable.

## Checks

```bash
cd coogpath
npm run build   # tsc -b + vite build
npm run lint    # oxlint, must report zero warnings
```
