import type { Session } from "../types/student";
import type { Term } from "../types/plan";
import { parseTermCode, toTermCode } from "../lib/terms";

const SESSION_KEY = "coogpath.session";
const START_TERM_KEY = "coogpath.startTerm";

// Keys written by the previous Next.js frontend; read once so signed-in users stay signed in.
const LEGACY_KEYS = [
  "studentId",
  "studentName",
  "programName",
  "capstoneChoice",
  "financeTrack",
  "mathMinor",
  "includeSummer",
  "startSemester",
  "freeElectiveCredits",
];

function migrateLegacySession(): Session | null {
  const legacyId = Number(localStorage.getItem("studentId"));
  const legacyName = localStorage.getItem("studentName");
  const legacyStart = localStorage.getItem("startSemester");
  if (legacyStart && parseTermCode(legacyStart)) {
    localStorage.setItem(START_TERM_KEY, legacyStart);
  }
  LEGACY_KEYS.forEach((key) => localStorage.removeItem(key));

  if (!Number.isInteger(legacyId) || legacyId <= 0) return null;
  const session = { studentId: legacyId, name: legacyName || "Student" };
  saveSession(session);
  return session;
}

export function loadSession(): Session | null {
  const raw = localStorage.getItem(SESSION_KEY);
  if (!raw) return migrateLegacySession();
  try {
    const parsed = JSON.parse(raw) as Partial<Session>;
    if (typeof parsed.studentId === "number" && typeof parsed.name === "string") {
      return { studentId: parsed.studentId, name: parsed.name };
    }
  } catch {
    // Fall through and treat a corrupt value as signed out.
  }
  localStorage.removeItem(SESSION_KEY);
  return null;
}

export function saveSession(session: Session): void {
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function clearSession(): void {
  localStorage.removeItem(SESSION_KEY);
  localStorage.removeItem(START_TERM_KEY);
}

export function loadStartTerm(): Term | null {
  const raw = localStorage.getItem(START_TERM_KEY);
  return raw ? parseTermCode(raw) : null;
}

export function saveStartTerm(term: Term): void {
  localStorage.setItem(START_TERM_KEY, toTermCode(term));
}
