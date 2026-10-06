import { useMemo, useState } from "react";
import { courseCode } from "../lib/format";
import type { Course } from "../types/course";

export interface SubjectGroup {
  subject: string;
  courses: Course[];
}

function compareCourses(a: Course, b: Course): number {
  return a.subject.localeCompare(b.subject) || a.number.localeCompare(b.number, undefined, { numeric: true });
}

/** Search text + subject filter over a course list, grouped by subject. */
export function useCourseFilter(courses: Course[]) {
  const [query, setQuery] = useState("");
  const [subject, setSubject] = useState<string | null>(null);

  const subjects = useMemo(() => {
    const counts = new Map<string, number>();
    for (const course of courses) counts.set(course.subject, (counts.get(course.subject) ?? 0) + 1);
    return [...counts.entries()]
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([name, count]) => ({ name, count }));
  }, [courses]);

  const groups = useMemo<SubjectGroup[]>(() => {
    const q = query.trim().toLowerCase();
    const matches = courses
      .filter((c) => !subject || c.subject === subject)
      .filter((c) => !q || courseCode(c).toLowerCase().includes(q) || c.title.toLowerCase().includes(q))
      .sort(compareCourses);

    const bySubject = new Map<string, Course[]>();
    for (const course of matches) {
      const list = bySubject.get(course.subject) ?? [];
      list.push(course);
      bySubject.set(course.subject, list);
    }
    return [...bySubject.entries()].map(([name, list]) => ({ subject: name, courses: list }));
  }, [courses, query, subject]);

  const resultCount = groups.reduce((sum, g) => sum + g.courses.length, 0);

  return { query, setQuery, subject, setSubject, subjects, groups, resultCount };
}
