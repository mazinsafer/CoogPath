import type { Course } from "../types/course";

export function pluralize(count: number, singular: string, plural = `${singular}s`): string {
  return `${count} ${count === 1 ? singular : plural}`;
}

export function initials(name: string): string {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]!.toUpperCase())
    .join("");
}

export function courseCode(course: Pick<Course, "subject" | "number">): string {
  return `${course.subject} ${course.number}`;
}

/**
 * Placeholder rows stand in for "any approved course" (e.g. COSC 4XXX-ELEC-1,
 * CORE LPC-3HR, ELEC GEN-ELEC-1). Real UH course numbers are four digits.
 */
export function isPlaceholderCourse(course: Pick<Course, "number">): boolean {
  return !/^\d{4}$/.test(course.number);
}

/** Free-elective rows the planner adds on the fly are not real catalog entries. */
export function isGeneratedElective(course: Pick<Course, "subject">): boolean {
  return course.subject === "ELEC";
}

export function percent(part: number, whole: number): number {
  if (whole <= 0) return 0;
  return Math.min(100, Math.round((part / whole) * 100));
}
