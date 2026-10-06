import { getCourses, getPrograms } from "../services/catalogService";
import { generatePlan, getRequirements } from "../services/planService";
import { getCompletedCourseIds, getProfile, getTranscript } from "../services/studentService";
import type { PlanOptions } from "../types/plan";
import { useAsync } from "./useAsync";

export function useStudentProfile(studentId: number) {
  return useAsync(() => getProfile(studentId), [studentId]);
}

export function usePlan(studentId: number, options: PlanOptions | null) {
  return useAsync(
    () => (options ? generatePlan(studentId, options) : Promise.resolve(null)),
    [studentId, options?.mode, options?.start.season, options?.start.year, options?.includeSummer],
  );
}

export function useRequirements(studentId: number) {
  return useAsync(() => getRequirements(studentId), [studentId]);
}

export function useTranscript(studentId: number) {
  return useAsync(() => getTranscript(studentId), [studentId]);
}

export function useCompletedCourseIds(studentId: number) {
  return useAsync(() => getCompletedCourseIds(studentId), [studentId]);
}

export function useCourses() {
  return useAsync(getCourses, []);
}

export function usePrograms() {
  return useAsync(getPrograms, []);
}
