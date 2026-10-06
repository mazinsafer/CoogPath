import { Logo } from "../components/layout/Logo";
import { SiteFooter } from "../components/layout/SiteFooter";
import { IconArrowRight, IconCheck } from "../components/Icons";
import { ThemeToggle } from "../components/ThemeToggle";
import { ButtonLink } from "../components/ui/Button";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { useSession } from "../hooks/useSession";

const EXAMPLE_TERMS = [
  {
    label: "Fall 2026",
    courses: [
      ["COSC 2436", "Programming and Data Structures", 4],
      ["MATH 2414", "Calculus II", 4],
      ["MATH 2305", "Discrete Mathematics", 3],
      ["ENGL 1302", "First Year Writing II", 3],
    ],
  },
  {
    label: "Spring 2027",
    courses: [
      ["COSC 2425", "Computer Organization and Architecture", 4],
      ["COSC 3320", "Algorithms and Data Structures", 3],
      ["MATH 2318", "Linear Algebra", 3],
      ["HIST 1302", "American History II", 3],
    ],
  },
] as const;

const STEPS = [
  {
    title: "Create an account",
    body: "Sign up with your UH email and choose your major.",
  },
  {
    title: "Mark what you've taken",
    body: "Check off completed courses and pick your capstone, track, or minor.",
  },
  {
    title: "Get a term-by-term plan",
    body: "CoogPath orders your remaining requirements by prerequisites and credit limits. Export it as a PDF for advising.",
  },
];

const PROGRAMS = [
  {
    name: "BS Computer Science",
    college: "College of Natural Sciences and Mathematics",
    options: ["Software Engineering capstone", "Data Science capstone", "Math minor"],
  },
  {
    name: "BBA Finance",
    college: "C.T. Bauer College of Business",
    options: ["Six finance tracks, including Real Estate and Global Energy Management", "Optional math minor"],
  },
];

const RULES = [
  "Prerequisite chains, including either-or prerequisites",
  "A credit cap per term, with summers optional",
  "Transfer and AP credit toward free electives",
  "Spreading your major's core courses across every term",
];

export function LandingPage() {
  useDocumentTitle("");
  const { session } = useSession();

  return (
    <div className="flex min-h-screen flex-col bg-surface">
      <header className="border-b border-zinc-200">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6">
          <Logo />
          <nav className="flex items-center gap-2">
            <ThemeToggle />
            {session ? (
              <ButtonLink to="/roadmap" size="sm">
                Open my roadmap
              </ButtonLink>
            ) : (
              <>
                <ButtonLink to="/login" variant="ghost" size="sm">
                  Sign in
                </ButtonLink>
                <ButtonLink to="/signup" size="sm">
                  Create account
                </ButtonLink>
              </>
            )}
          </nav>
        </div>
      </header>

      <main className="flex-1">
        <section className="mx-auto grid max-w-6xl items-center gap-12 px-4 py-16 sm:px-6 lg:grid-cols-[1fr_minmax(0,460px)] lg:py-24">
          <div>
            <p className="text-sm font-medium text-brand-700">Degree planning for University of Houston students</p>
            <h1 className="mt-3 max-w-xl text-4xl font-semibold tracking-tight text-balance text-zinc-900 sm:text-5xl">
              Every semester, planned through graduation.
            </h1>
            <p className="mt-5 max-w-lg text-lg leading-relaxed text-zinc-600">
              Tell CoogPath which courses you've finished. It builds a term-by-term schedule for the rest of your degree,
              in an order your prerequisites allow.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <ButtonLink to={session ? "/roadmap" : "/signup"} icon={null}>
                {session ? "Open my roadmap" : "Build my plan"}
                <IconArrowRight />
              </ButtonLink>
              {!session && (
                <ButtonLink to="/login" variant="secondary">
                  I already have an account
                </ButtonLink>
              )}
            </div>
          </div>

          <figure className="overflow-hidden rounded-xl border border-zinc-200 bg-zinc-50 shadow-sm">
            <figcaption className="flex items-center justify-between border-b border-zinc-200 bg-surface px-4 py-3 text-xs">
              <span className="font-medium text-zinc-900">Example plan</span>
              <span className="text-zinc-500">BS Computer Science</span>
            </figcaption>
            <div className="space-y-3 p-4">
              {EXAMPLE_TERMS.map((term) => (
                <div key={term.label} className="rounded-lg border border-zinc-200 bg-surface">
                  <div className="flex items-center justify-between border-b border-zinc-100 px-3.5 py-2 text-xs">
                    <span className="font-semibold text-zinc-900">{term.label}</span>
                    <span className="text-zinc-500">
                      {term.courses.reduce((sum, [, , credits]) => sum + credits, 0)} credits
                    </span>
                  </div>
                  <ul className="divide-y divide-zinc-100 text-xs">
                    {term.courses.map(([code, title, credits]) => (
                      <li key={code} className="flex items-center gap-3 px-3.5 py-2">
                        <span className="w-20 shrink-0 font-medium text-zinc-900">{code}</span>
                        <span className="min-w-0 flex-1 truncate text-zinc-600">{title}</span>
                        <span className="text-zinc-400 tabular-nums">{credits}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
          </figure>
        </section>

        <section className="border-t border-zinc-200 bg-zinc-50">
          <div className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
            <h2 className="text-sm font-semibold text-zinc-900">How it works</h2>
            <ol className="mt-6 grid gap-6 md:grid-cols-3">
              {STEPS.map((step, i) => (
                <li key={step.title} className="flex gap-4">
                  <span className="flex size-7 shrink-0 items-center justify-center rounded-full border border-zinc-300 bg-surface text-sm font-medium text-zinc-700">
                    {i + 1}
                  </span>
                  <div>
                    <h3 className="text-sm font-semibold text-zinc-900">{step.title}</h3>
                    <p className="mt-1 text-sm leading-relaxed text-zinc-600">{step.body}</p>
                  </div>
                </li>
              ))}
            </ol>
          </div>
        </section>

        <section className="mx-auto grid max-w-6xl gap-12 px-4 py-16 sm:px-6 lg:grid-cols-2">
          <div>
            <h2 className="text-sm font-semibold text-zinc-900">Supported programs</h2>
            <p className="mt-1 text-sm text-zinc-500">2024 catalog year. More majors are on the way.</p>
            <div className="mt-6 space-y-3">
              {PROGRAMS.map((program) => (
                <div key={program.name} className="rounded-lg border border-zinc-200 p-5">
                  <h3 className="text-sm font-semibold text-zinc-900">{program.name}</h3>
                  <p className="text-sm text-zinc-500">{program.college}</p>
                  <ul className="mt-3 space-y-1 text-sm text-zinc-600">
                    {program.options.map((option) => (
                      <li key={option}>{option}</li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
          </div>
          <div>
            <h2 className="text-sm font-semibold text-zinc-900">What the planner accounts for</h2>
            <ul className="mt-6 space-y-3">
              {RULES.map((rule) => (
                <li key={rule} className="flex gap-3 text-sm text-zinc-700">
                  <IconCheck className="mt-0.5 size-4 shrink-0 text-brand-600" />
                  {rule}
                </li>
              ))}
            </ul>
          </div>
        </section>
      </main>

      <SiteFooter />
    </div>
  );
}
