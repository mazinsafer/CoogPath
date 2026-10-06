import { PageHeader } from "../components/layout/PageHeader";
import { Badge } from "../components/ui/Badge";
import { ButtonLink } from "../components/ui/Button";
import { Card } from "../components/ui/Card";
import { EmptyState, ErrorState, LoadingState } from "../components/ui/States";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { useRequiredSession } from "../hooks/useSession";
import { useTranscript } from "../hooks/useStudentData";
import { pluralize } from "../lib/format";
import type { TranscriptStatus } from "../types/student";

const STATUS: Record<TranscriptStatus, { label: string; tone: "success" | "info" | "warning" | "neutral" }> = {
  TAKEN: { label: "Completed", tone: "success" },
  IN_PROGRESS: { label: "In progress", tone: "info" },
  TRANSFER: { label: "Transfer", tone: "warning" },
  PLANNED: { label: "Planned", tone: "neutral" },
};

export function TranscriptPage() {
  useDocumentTitle("Transcript");
  const { studentId } = useRequiredSession();
  const transcript = useTranscript(studentId);
  const entries = transcript.data ?? [];
  const credits = entries.reduce((sum, e) => sum + e.credits, 0);

  return (
    <>
      <PageHeader
        title="Transcript"
        description={
          transcript.data ? `${pluralize(entries.length, "course")} · ${credits} credits` : "Courses you've marked as completed."
        }
        actions={
          <ButtonLink to="/setup" variant="secondary" size="sm">
            Update courses
          </ButtonLink>
        }
      />

      <div className="py-6">
        {transcript.error ? (
          <ErrorState message={transcript.error} onRetry={transcript.reload} />
        ) : !transcript.data ? (
          <LoadingState label="Loading transcript…" />
        ) : entries.length === 0 ? (
          <Card>
            <EmptyState
              title="No courses yet"
              description="Courses you mark as completed will appear here."
              action={<ButtonLink to="/setup">Add completed courses</ButtonLink>}
            />
          </Card>
        ) : (
          <Card className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-zinc-200 text-left text-xs font-medium text-zinc-500">
                  <th className="px-5 py-3 font-medium">Course</th>
                  <th className="px-3 py-3 font-medium">Title</th>
                  <th className="px-3 py-3 text-right font-medium">Credits</th>
                  <th className="px-3 py-3 font-medium">Grade</th>
                  <th className="px-5 py-3 font-medium">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-zinc-100">
                {entries.map((entry) => {
                  const status = STATUS[entry.status] ?? STATUS.PLANNED;
                  return (
                    <tr key={entry.courseId}>
                      <td className="px-5 py-2.5 font-medium whitespace-nowrap text-zinc-900">{entry.courseCode}</td>
                      <td className="px-3 py-2.5 text-zinc-600">{entry.title}</td>
                      <td className="px-3 py-2.5 text-right text-zinc-500 tabular-nums">{entry.credits}</td>
                      <td className="px-3 py-2.5 text-zinc-500">{entry.grade ?? "—"}</td>
                      <td className="px-5 py-2.5">
                        <Badge tone={status.tone}>{status.label}</Badge>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </Card>
        )}
      </div>
    </>
  );
}
