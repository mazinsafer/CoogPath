import type { Course, DegreeProgram } from "../types/course";
import { api } from "./api";

export function getCourses(): Promise<Course[]> {
  return api.get<Course[]>("/courses");
}

export function getPrograms(): Promise<DegreeProgram[]> {
  return api.get<DegreeProgram[]>("/programs");
}
