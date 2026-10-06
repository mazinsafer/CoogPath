import type { Session } from "../types/student";
import type { Term } from "../types/plan";
import { parseTermCode, toTermCode } from "../lib/terms";

const SESSION_KEY = "coogpath.session";
const START_TERM_KEY = "coogpath.startTerm";

// Keys written by the previous Next.js frontend. They carry no sign-in token, so
// those users sign in again; only their chosen start term is kept.
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

function clearLegacyKeys(): void {
  const legacyStart = localStorage.getItem("startSemester");
  if (legacyStart && parseTermCode(legacyStart)) {
    localStorage.setItem(START_TERM_KEY, legacyStart);
  }
  LEGACY_KEYS.forEach((key) => localStorage.removeItem(key));
}

function isUsable(session: Partial<Session>): session is Session {
  return (
    typeof session.studentId === "number" &&
    typeof session.name === "string" &&
    typeof session.token === "string" &&
    typeof session.expiresAt === "string" &&
    Date.parse(session.expiresAt) > Date.now()
  );
}

export function loadSession(): Session | null {
  const raw = localStorage.getItem(SESSION_KEY);
  if (!raw) {
    clearLegacyKeys();
    return null;
  }
  try {
    const parsed = JSON.parse(raw) as Partial<Session>;
    if (isUsable(parsed)) {
      return { studentId: parsed.studentId, name: parsed.name, token: parsed.token, expiresAt: parsed.expiresAt };
    }
  } catch {
    // Fall through and treat a corrupt value as signed out.
  }
  localStorage.removeItem(SESSION_KEY);
  return null;
}

/** The bearer token for API requests, or null when signed out or expired. */
export function loadToken(): string | null {
  return loadSession()?.token ?? null;
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
