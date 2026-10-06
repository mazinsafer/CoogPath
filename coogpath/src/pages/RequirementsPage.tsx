import { PageHeader } from "../components/layout/PageHeader";
import { IconCheck } from "../components/Icons";
import { Badge } from "../components/ui/Badge";
import { ButtonLink } from "../components/ui/Button";
import { Card } from "../components/ui/Card";
import { ProgressBar } from "../components/ui/ProgressBar";
import { EmptyState, ErrorState, LoadingState } from "../components/ui/States";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { useRequiredSession } from "../hooks/useSession";
import { useRequirements } from "../hooks/useStudentData";
import { percent } from "../lib/format";
import type { RequirementGroup } from "../types/requirement";

export function RequirementsPage() {
  useDocumentTitle("Requirements");
  const { studentId } = useRequiredSession();
  const requirements = useRequirements(studentId);
  const groups = requirements.data ?? [];

  const total = groups.reduce((sum, g) => sum + g.totalCredits, 0);
  const completed = groups.reduce((sum, g) => sum + Math.min(g.completedCredits, g.totalCredits), 0);
  const pct = percent(completed, total);

  return (
    <>
      <PageHeader
        title="Requirements"
        description="Progress toward each requirement in your degree, based on the courses you've marked complete."
        actions={
          <ButtonLink to="/setup" variant="secondary" size="sm">
            Update courses
          </ButtonLink>
        }
      />

      {requirements.error ? (
        <ErrorState message={requirements.error} onRetry={requirements.reload} />
      ) : !requirements.data ? (
        <LoadingState label="Loading requirements…" />
      ) : groups.length === 0 ? (
        <EmptyState title="No requirements found" description="Your account isn't linked to a supported degree program." />
      ) : (
        <div className="space-y-5 py-6">
          <Card className="p-5">
            <div className="flex items-baseline justify-between gap-4">
              <div>
                <p className="text-sm font-medium text-zinc-900">Overall progress</p>
                <p className="mt-0.5 text-sm text-zinc-500">
                  {completed} of {total} credits
                </p>
              </div>
              <p className="text-2xl font-semibold tracking-tight text-zinc-900 tabular-nums">{pct}%</p>
            </div>
            <div className="mt-4">
              <ProgressBar value={pct} label="Overall degree progress" />
            </div>
          </Card>

          <div className="grid gap-5 lg:grid-cols-2">
            {groups.map((group) => (
              <RequirementCard key={group.name} group={group} />
            ))}
          </div>
        </div>
      )}
    </>
  );
}

function RequirementCard({ group }: { group: RequirementGroup }) {
  const pct = percent(group.completedCredits, group.totalCredits);
  const done = pct >= 100;

  return (
    <Card className="flex flex-col">
      <header className="border-b border-zinc-200 px-5 py-4">
        <div className="flex items-center justify-between gap-3">
          <h2 className="text-sm font-semibold text-zinc-900">{group.name}</h2>
          {done ? (
            <Badge tone="success">Complete</Badge>
          ) : (
            <span className="text-xs text-zinc-500 tabular-nums">
              {group.completedCredits} / {group.totalCredits} cr
            </span>
          )}
        </div>
        <div className="mt-3">
          <ProgressBar value={pct} label={`${group.name} progress`} tone={done ? "success" : "brand"} />
        </div>
      </header>
      <ul className="divide-y divide-zinc-100">
        {group.courses.map((course) => (
          <li key={course.courseCode} className="flex items-center gap-3 px-5 py-2.5 text-sm">
            <span
              className={`flex size-4 shrink-0 items-center justify-center rounded-full ${
                course.completed ? "bg-emerald-600 text-white" : "border border-zinc-300"
              }`}
              aria-label={course.completed ? "Completed" : "Not completed"}
              role="img"
            >
              {course.completed && <IconCheck className="size-2.5" />}
            </span>
            <span className={`shrink-0 font-medium ${course.completed ? "text-zinc-500" : "text-zinc-900"}`}>
              {course.courseCode}
            </span>
            <span className="min-w-0 flex-1 truncate text-zinc-500">{course.title}</span>
            <span className="shrink-0 text-zinc-400 tabular-nums">{course.credits}</span>
          </li>
        ))}
      </ul>
    </Card>
  );
}
