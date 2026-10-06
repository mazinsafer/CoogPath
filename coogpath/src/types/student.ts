export type CapstoneChoice = "SENIOR_SE" | "SENIOR_DS" | "MATH_MINOR";

export type FinanceTrack = "STANDARD" | "RE" | "PFP" | "CBC" | "GEM" | "ECTC";

export interface StudentProfile {
  studentId: number;
  name: string;
  email: string;
  programId: number | null;
  programName: string | null;
  capstoneChoice: CapstoneChoice;
  financeTrack: FinanceTrack;
  mathMinor: boolean;
  freeElectiveCredits: number;
  includeSummer: boolean;
}

/** Partial update sent to PATCH /students/{id}/preferences. Omitted fields are unchanged. */
export type StudentPreferences = Partial<
  Pick<StudentProfile, "capstoneChoice" | "financeTrack" | "mathMinor" | "freeElectiveCredits" | "includeSummer">
>;

export interface RegistrationRequest {
  name: string;
  email: string;
  password: string;
  programId: number;
  catalogYear: number;
}

export type TranscriptStatus = "TAKEN" | "IN_PROGRESS" | "PLANNED" | "TRANSFER";

export interface TranscriptEntry {
  courseId: number;
  courseCode: string;
  title: string;
  credits: number;
  status: TranscriptStatus;
  grade: string | null;
}

/** Returned by login and registration. */
export interface AuthResponse {
  token: string;
  /** ISO-8601 instant after which the API rejects the token. */
  expiresAt: string;
  student: StudentProfile;
}

/** What the browser remembers about the signed-in student. */
export interface Session {
  studentId: number;
  name: string;
  token: string;
  expiresAt: string;
}
