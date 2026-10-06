import type { AuthResponse } from "../types/student";
import { api } from "./api";

export function login(email: string, password: string): Promise<AuthResponse> {
  return api.post<AuthResponse>("/auth/login", { email: email.trim(), password });
}
