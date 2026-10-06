import type {
  RegistrationRequest,
  StudentPreferences,
  StudentProfile,
  TranscriptEntry,
} from "../types/student";
import { api } from "./api";

export function register(request: RegistrationRequest): Promise<StudentProfile> {
  return api.post<StudentProfile>("/students/register", request);
}

export function getProfile(studentId: number): Promise<StudentProfile> {
  return api.get<StudentProfile>(`/students/${studentId}`);
}

export function updatePreferences(studentId: number, preferences: StudentPreferences): Promise<StudentProfile> {
  return api.patch<StudentProfile>(`/students/${studentId}/preferences`, preferences);
}

export function getTranscript(studentId: number): Promise<TranscriptEntry[]> {
  return api.get<TranscriptEntry[]>(`/students/${studentId}/transcript`);
}

export function getCompletedCourseIds(studentId: number): Promise<number[]> {
  return api.get<number[]>(`/students/${studentId}/courses`);
}

/** Replaces the student's completed courses with exactly this set. */
export function saveCompletedCourses(studentId: number, courseIds: number[]): Promise<{ saved: number }> {
  return api.put<{ saved: number }>(`/students/${studentId}/courses`, courseIds);
}
