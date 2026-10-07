import { api } from "./api";
import type { PlanOptions } from "../types/plan";

export interface AdvisorMessage {
  role: "user" | "assistant";
  content: string;
}

export function askAdvisor(studentId: number, question: string, options: PlanOptions, history: AdvisorMessage[]) {
  return api.post<{ answer: string }>(`/advisor/${studentId}`, {
    question,
    mode: options.mode,
    startSeason: options.start.season,
    startYear: options.start.year,
    includeSummer: options.includeSummer,
    history: history.slice(-6),
  });
}
