import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CourseFilters } from "../components/courses/CourseFilters";
import { PageHeader } from "../components/layout/PageHeader";
import { OptionGroup } from "../components/setup/OptionGroup";
import { TermSelect } from "../components/TermSelect";
import { Alert } from "../components/ui/Alert";
import { Badge } from "../components/ui/Badge";
import { Button } from "../components/ui/Button";
import { Card, CardHeader } from "../components/ui/Card";
import { EmptyState, ErrorState, LoadingState } from "../components/ui/States";
import { Toggle } from "../components/ui/Toggle";
import { useCourseFilter } from "../hooks/useCourseFilter";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { useRequiredSession } from "../hooks/useSession";
import { useCompletedCourseIds, useCourses, useStudentProfile } from "../hooks/useStudentData";
import { isGeneratedElective, isPlaceholderCourse, pluralize } from "../lib/format";
import { CAPSTONE_OPTIONS, FINANCE_TRACK_OPTIONS, isComputerScience, isFinance } from "../lib/programs";
import { defaultStartTerm } from "../lib/terms";
import { loadStartTerm, saveStartTerm } from "../services/sessionStore";
import { saveCompletedCourses, updatePreferences } from "../services/studentService";
import type { Course } from "../types/course";
import type { Term } from "../types/plan";
import type { StudentProfile } from "../types/student";

const MAX_TRANSFER_CREDITS = 60;

export function SetupPage() {
  useDocumentTitle("Courses & options");
  const { studentId } = useRequiredSession();
  const profile = useStudentProfile(studentId);
  const courses = useCourses();
  const completed = useCompletedCourseIds(studentId);

  const error = profile.error ?? courses.error ?? completed.error;
  const ready = profile.data && courses.data && completed.data;

  return (
    <>
      <PageHeader
        title="Courses & options"
        description="Mark everything you've completed and choose your degree options. Your roadmap is built from this."
      />
      {error ? (
        <ErrorState
          message={error}
          onRetry={() => {
            profile.reload();
            courses.reload();
            completed.reload();
          }}
        />
      ) : !ready ? (
        <LoadingState label="Loading your courses…" />
      ) : (
        <SetupForm profile={profile.data!} courses={courses.data!} completedIds={completed.data!} />
      )}
    </>
  );
}

interface SetupFormProps {
  profile: StudentProfile;
  courses: Course[];
  completedIds: number[];
}

function SetupForm({ profile, courses, completedIds }: SetupFormProps) {
  const navigate = useNavigate();
  const isCs = isComputerScience(profile.programId);
  const isFin = isFinance(profile.programId);

  const [capstoneChoice, setCapstoneChoice] = useState(profile.capstoneChoice);
  const [financeTrack, setFinanceTrack] = useState(profile.financeTrack);
  const [mathMinor, setMathMinor] = useState(profile.mathMinor);
  const [includeSummer, setIncludeSummer] = useState(profile.includeSummer);
  const [transferCredits, setTransferCredits] = useState(profile.freeElectiveCredits);
  const [startTerm, setStartTerm] = useState<Term>(() => loadStartTerm() ?? defaultStartTerm());
  const [selected, setSelected] = useState(() => new Set(completedIds));
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  const selectableCourses = useMemo(() => courses.filter((c) => !isGeneratedElective(c)), [courses]);
  const filter = useCourseFilter(selectableCourses);

  const selectedCredits = useMemo(
    () => selectableCourses.filter((c) => selected.has(c.courseId)).reduce((sum, c) => sum + c.credits, 0),
    [selectableCourses, selected],
  );

  const toggleCourse = (courseId: number) => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(courseId)) next.delete(courseId);
      else next.add(courseId);
      return next;
    });
  };

  const handleSave = async () => {
    setSaving(true);
    setSaveError(null);
    try {
      await updatePreferences(profile.studentId, {
        includeSummer,
        freeElectiveCredits: transferCredits,
        ...(isCs && { capstoneChoice }),
        ...(isFin && { financeTrack, mathMinor }),
      });
      await saveCompletedCourses(profile.studentId, [...selected]);
      saveStartTerm(startTerm);
      navigate("/roadmap");
    } catch (err) {
      setSaveError(err instanceof Error ? err.message : "Couldn't save your changes.");
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6 pt-6 pb-24">
      {(isCs || isFin) && (
        <Card>
          <CardHeader
            title={isCs ? "Senior sequence or minor" : "Finance track"}
            description={
              isCs
                ? "BS Computer Science students complete one of these to finish the degree."
                : "Your roadmap includes the required courses and electives for the track you choose."
            }
          />
          <div className="space-y-5 p-5">
            {isCs && (
              <OptionGroup
                name="capstone"
                label="Senior sequence or minor"
                options={CAPSTONE_OPTIONS}
                value={capstoneChoice}
                onChange={setCapstoneChoice}
              />
            )}
            {isFin && (
              <>
                <OptionGroup
                  name="financeTrack"
                  label="Finance track"
                  options={FINANCE_TRACK_OPTIONS}
                  value={financeTrack}
                  onChange={setFinanceTrack}
                />
                <div className="border-t border-zinc-100 pt-5">
                  <Toggle
                    checked={mathMinor}
                    onChange={setMathMinor}
                    label="Add a math minor"
                    description="Adds Calculus I and II plus three upper-division math courses, which also fill general elective slots."
                  />
                </div>
              </>
            )}
          </div>
        </Card>
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader title="Schedule" description="When your plan starts and whether it can use summers." />
          <div className="space-y-5 p-5">
            <TermSelect label="First term to plan" value={startTerm} onChange={setStartTerm} />
            <Toggle
              checked={includeSummer}
              onChange={setIncludeSummer}
              label="Include summer terms"
              description="Summer terms are capped at 6 credits."
            />
          </div>
        </Card>

        <Card>
          <CardHeader
            title="Transfer and AP credit"
            description="Credit that doesn't match a specific UH course counts toward free electives."
          />
          <div className="p-5">
            <label htmlFor="transfer-credits" className="mb-1.5 block text-sm font-medium text-zinc-800">
              Free elective credit hours already earned
            </label>
            <input
              id="transfer-credits"
              type="number"
              inputMode="numeric"
              min={0}
              max={MAX_TRANSFER_CREDITS}
              value={transferCredits}
              onChange={(e) =>
                setTransferCredits(Math.max(0, Math.min(MAX_TRANSFER_CREDITS, Number(e.target.value) || 0)))
              }
              className="block h-10 w-28 rounded-md border border-zinc-300 bg-white px-3 text-sm tabular-nums shadow-sm focus:border-brand-600 focus:ring-2 focus:ring-brand-600/15 focus:outline-none"
            />
          </div>
        </Card>
      </div>

      <Card>
        <CardHeader
          title="Completed courses"
          description="Rows marked “Any approved course” stand in for a requirement you can fill with several courses."
        />
        <div className="space-y-6 p-5">
          <CourseFilters
            query={filter.query}
            onQueryChange={filter.setQuery}
            subject={filter.subject}
            onSubjectChange={filter.setSubject}
            subjects={filter.subjects}
            total={selectableCourses.length}
          />
          {filter.groups.length === 0 ? (
            <EmptyState title="No matching courses" description="Try a different course code or title." />
          ) : (
            filter.groups.map((group) => (
              <fieldset key={group.subject}>
                <legend className="flex w-full items-center justify-between pb-2 text-xs font-medium text-zinc-500">
                  <span>{group.subject}</span>
                  <span className="tabular-nums">
                    {group.courses.filter((c) => selected.has(c.courseId)).length} of {group.courses.length} selected
                  </span>
                </legend>
                <div className="divide-y divide-zinc-100 rounded-md border border-zinc-200">
                  {group.courses.map((course) => (
                    <label
                      key={course.courseId}
                      className="flex cursor-pointer items-center gap-3 px-3.5 py-2.5 hover:bg-zinc-50"
                    >
                      <input
                        type="checkbox"
                        checked={selected.has(course.courseId)}
                        onChange={() => toggleCourse(course.courseId)}
                        className="size-4 shrink-0 rounded accent-brand-600"
                      />
                      <span className="w-36 shrink-0 text-sm font-medium text-zinc-900">
                        {course.subject} {course.number}
                      </span>
                      <span className="min-w-0 flex-1 truncate text-sm text-zinc-600">{course.title}</span>
                      {isPlaceholderCourse(course) && (
                        <span className="hidden sm:inline">
                          <Badge>Any approved course</Badge>
                        </span>
                      )}
                      <span className="w-10 shrink-0 text-right text-sm text-zinc-500 tabular-nums">{course.credits}</span>
                    </label>
                  ))}
                </div>
              </fieldset>
            ))
          )}
        </div>
      </Card>

      <div className="fixed inset-x-0 bottom-0 z-20 border-t border-zinc-200 bg-white/95 backdrop-blur lg:left-60">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-3 px-4 py-3 sm:px-6 lg:px-10">
          <p className="text-sm text-zinc-600">
            <span className="font-medium text-zinc-900">{pluralize(selected.size, "course")}</span> ·{" "}
            {selectedCredits + transferCredits} credits completed
          </p>
          <div className="flex items-center gap-3">
            {saveError && <Alert tone="error">{saveError}</Alert>}
            <Button onClick={handleSave} loading={saving}>
              Save and view roadmap
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
