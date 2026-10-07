import { useState } from "react";
import { PageHeader } from "../components/layout/PageHeader";
import { TermCard } from "../components/roadmap/TermCard";
import { AdvisorChat } from "../components/roadmap/AdvisorChat";
import { TermSelect } from "../components/TermSelect";
import { IconDownload } from "../components/Icons";
import { Alert } from "../components/ui/Alert";
import { Button, ButtonLink } from "../components/ui/Button";
import { SegmentedControl } from "../components/ui/SegmentedControl";
import { StatGrid } from "../components/ui/StatGrid";
import { EmptyState, ErrorState, LoadingState } from "../components/ui/States";
import { Toggle } from "../components/ui/Toggle";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { useRequiredSession } from "../hooks/useSession";
import { useCompletedCourseIds, usePlan, useStudentProfile } from "../hooks/useStudentData";
import { pluralize } from "../lib/format";
import { defaultStartTerm, formatTermLabel } from "../lib/terms";
import { exportRoadmapPdf } from "../services/pdfExport";
import { loadStartTerm, saveStartTerm } from "../services/sessionStore";
import { updatePreferences } from "../services/studentService";
import type { PlanMode, PlanOptions, Term } from "../types/plan";
import type { StudentProfile } from "../types/student";

const MODE_OPTIONS: { value: PlanMode; label: string }[] = [
  { value: "fastest", label: "Fastest" },
  { value: "balanced", label: "Balanced" },
];

const MODE_HINTS: Record<PlanMode, string> = {
  fastest: "Up to 18 credits each fall and spring.",
  balanced: "Up to 16 credits each fall and spring, with fewer heavy STEM courses per term.",
};

export function RoadmapPage() {
  useDocumentTitle("Roadmap");
  const { studentId } = useRequiredSession();
  const profile = useStudentProfile(studentId);

  if (profile.error) return <ErrorState message={profile.error} onRetry={profile.reload} />;
  if (!profile.data) return <LoadingState label="Loading your roadmap…" />;
  return <Roadmap profile={profile.data} />;
}

function Roadmap({ profile }: { profile: StudentProfile }) {
  const [options, setOptions] = useState<PlanOptions>(() => ({
    mode: "fastest",
    start: loadStartTerm() ?? defaultStartTerm(),
    includeSummer: profile.includeSummer,
  }));
  const [exporting, setExporting] = useState(false);
  const plan = usePlan(profile.studentId, options);
  const completed = useCompletedCourseIds(profile.studentId);

  const setStart = (start: Term) => {
    saveStartTerm(start);
    setOptions((o) => ({ ...o, start }));
  };

  const setIncludeSummer = (includeSummer: boolean) => {
    setOptions((o) => ({ ...o, includeSummer }));
    // Persisting is best-effort; the plan already reflects the new choice.
    updatePreferences(profile.studentId, { includeSummer }).catch(() => undefined);
  };

  const handleExport = async () => {
    if (!plan.data) return;
    setExporting(true);
    try {
      await exportRoadmapPdf(plan.data, profile.name, profile.programName);
    } finally {
      setExporting(false);
    }
  };

  const data = plan.data;
  const credits = data?.terms.reduce((sum, t) => sum + t.totalCredits, 0) ?? 0;
  const courseCount = data?.terms.reduce((sum, t) => sum + t.courses.length, 0) ?? 0;
  const lastTerm = data?.terms.at(-1);

  return (
    <>
      <PageHeader
        title="Roadmap"
        description={profile.programName ?? undefined}
        actions={
          <Button variant="secondary" size="sm" icon={<IconDownload />} onClick={handleExport} loading={exporting} disabled={!data?.terms.length}>
            Export PDF
          </Button>
        }
      />

      <div className="flex flex-col gap-4 py-5 md:flex-row md:items-center md:justify-between">
        <div className="flex flex-wrap items-center gap-3">
          <SegmentedControl
            label="Plan mode"
            value={options.mode}
            options={MODE_OPTIONS}
            onChange={(mode) => setOptions((o) => ({ ...o, mode }))}
          />
          <span className="text-sm text-zinc-500">{MODE_HINTS[options.mode]}</span>
        </div>
        <div className="flex flex-wrap items-center gap-5">
          <label className="flex items-center gap-2 text-sm text-zinc-600">
            Starting
            <TermSelect value={options.start} onChange={setStart} />
          </label>
          <Toggle checked={options.includeSummer} onChange={setIncludeSummer} label="Summers" />
        </div>
      </div>

      {completed.data?.length === 0 && (
        <div className="mb-5">
          <Alert
            tone="info"
            title="No completed courses yet"
            action={
              <ButtonLink to="/setup" variant="secondary" size="sm">
                Add courses
              </ButtonLink>
            }
          >
            This plan assumes you're starting from scratch. Mark what you've finished for an accurate roadmap.
          </Alert>
        </div>
      )}

      {plan.error && !data ? (
        <ErrorState message={plan.error} onRetry={plan.reload} />
      ) : !data ? (
        <LoadingState label="Building your plan…" />
      ) : (
        <div className={`space-y-5 transition-opacity ${plan.loading ? "opacity-60" : ""}`} aria-busy={plan.loading}>
          <StatGrid
            stats={[
              { label: "Credits remaining", value: credits },
              { label: "Terms remaining", value: data.terms.length, detail: options.includeSummer ? "Including summers" : "Fall and spring" },
              { label: "Courses remaining", value: courseCount },
              { label: "Estimated graduation", value: lastTerm ? formatTermLabel(lastTerm.termLabel) : "—" },
            ]}
          />

          {data.blockers.length > 0 && (
            <Alert tone="warning" title={`${pluralize(data.blockers.length, "course")} couldn't be scheduled`}>
              <ul className="list-disc space-y-0.5 pl-4">
                {data.blockers.map((blocker) => (
                  <li key={blocker}>{blocker}</li>
                ))}
              </ul>
            </Alert>
          )}

          {data.terms.length === 0 ? (
            <EmptyState
              title="Nothing left to schedule"
              description="Every requirement in your program is marked complete."
            />
          ) : (
            data.terms.map((term, i) => <TermCard key={term.termLabel} term={term} index={i} />)
          )}

          {!plan.loading && (
            <AdvisorChat
              key={`${profile.studentId}-${options.mode}-${options.start.season}-${options.start.year}-${options.includeSummer}`}
              studentId={profile.studentId}
              options={options}
            />
          )}

          <p className="text-xs text-zinc-500">
            Generated plans don't account for course availability by term or registration holds. Review your plan with
            an academic advisor.
          </p>
        </div>
      )}
    </>
  );
}
