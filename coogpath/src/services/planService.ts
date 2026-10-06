import type { PlanOptions, PlanResult } from "../types/plan";
import type { RequirementGroup } from "../types/requirement";
import { api } from "./api";

export function generatePlan(studentId: number, options: PlanOptions): Promise<PlanResult> {
  const params = new URLSearchParams({
    mode: options.mode,
    startSeason: options.start.season,
    startYear: String(options.start.year),
    includeSummer: String(options.includeSummer),
  });
  return api.get<PlanResult>(`/plan/generate/${studentId}?${params}`);
}

export function getRequirements(studentId: number): Promise<RequirementGroup[]> {
  return api.get<RequirementGroup[]>(`/requirements/${studentId}`);
}
