import type { StudentProfile } from "../types/student";
import { api } from "./api";

export function login(email: string, password: string): Promise<StudentProfile> {
  return api.post<StudentProfile>("/auth/login", { email: email.trim(), password });
}
