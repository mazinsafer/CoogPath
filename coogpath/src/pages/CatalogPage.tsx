import { useMemo } from "react";
import { CourseFilters } from "../components/courses/CourseFilters";
import { PageHeader } from "../components/layout/PageHeader";
import { Badge } from "../components/ui/Badge";
import { Card } from "../components/ui/Card";
import { EmptyState, ErrorState, LoadingState } from "../components/ui/States";
import { useCourseFilter } from "../hooks/useCourseFilter";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { useCourses } from "../hooks/useStudentData";
import { isGeneratedElective, isPlaceholderCourse } from "../lib/format";

export function CatalogPage() {
  useDocumentTitle("Course catalog");
  const courses = useCourses();
  const catalog = useMemo(() => (courses.data ?? []).filter((c) => !isGeneratedElective(c)), [courses.data]);
  const filter = useCourseFilter(catalog);

  return (
    <>
      <PageHeader
        title="Course catalog"
        description="Every course CoogPath knows about for the supported degree programs."
      />

      {courses.error ? (
        <ErrorState message={courses.error} onRetry={courses.reload} />
      ) : !courses.data ? (
        <LoadingState label="Loading catalog…" />
      ) : (
        <div className="space-y-6 py-6">
          <CourseFilters
            query={filter.query}
            onQueryChange={filter.setQuery}
            subject={filter.subject}
            onSubjectChange={filter.setSubject}
            subjects={filter.subjects}
            total={catalog.length}
          />

          {filter.groups.length === 0 ? (
            <EmptyState title="No matching courses" description="Try a different course code or title." />
          ) : (
            filter.groups.map((group) => (
              <section key={group.subject}>
                <h2 className="pb-2 text-xs font-medium text-zinc-500">
                  {group.subject} <span className="text-zinc-400">· {group.courses.length}</span>
                </h2>
                <Card className="overflow-hidden">
                  <table className="w-full text-sm">
                    <thead className="sr-only">
                      <tr>
                        <th>Course</th>
                        <th>Title</th>
                        <th>Credits</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-zinc-100">
                      {group.courses.map((course) => (
                        <tr key={course.courseId}>
                          <td className="w-40 py-2.5 pr-3 pl-5 font-medium whitespace-nowrap text-zinc-900">
                            {course.subject} {course.number}
                          </td>
                          <td className="py-2.5 pr-3 text-zinc-600">
                            {course.title}
                            {isPlaceholderCourse(course) && (
                              <span className="ml-2 hidden sm:inline">
                                <Badge>Any approved course</Badge>
                              </span>
                            )}
                          </td>
                          <td className="w-16 py-2.5 pr-5 text-right text-zinc-500 tabular-nums">{course.credits}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </Card>
              </section>
            ))
          )}
        </div>
      )}
    </>
  );
}
