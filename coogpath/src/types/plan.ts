export type PlanMode = "fastest" | "balanced";

export type Season = "SPRING" | "SUMMER" | "FALL";

export interface Term {
  season: Season;
  year: number;
}

export interface PlannedCourse {
  courseCode: string;
  title: string;
  credits: number;
  prereqString: string;
  reason: string;
}

export interface PlannedTerm {
  /** Uppercase label from the API, e.g. "FALL 2026". Format with formatTermLabel. */
  termLabel: string;
  season: Season;
  year: number;
  totalCredits: number;
  courses: PlannedCourse[];
}

export interface PlanResult {
  terms: PlannedTerm[];
  unmetRequirements: string[];
  blockers: string[];
}

export interface PlanOptions {
  mode: PlanMode;
  start: Term;
  includeSummer: boolean;
}
