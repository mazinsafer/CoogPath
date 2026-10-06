import { useCallback, useMemo, useState, type ReactNode } from "react";
import { clearSession, loadSession, saveSession } from "../services/sessionStore";
import type { Session, StudentProfile } from "../types/student";
import { SessionContext } from "./sessionContext";

export function SessionProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(() => loadSession());

  const signIn = useCallback((profile: StudentProfile) => {
    const next = { studentId: profile.studentId, name: profile.name };
    saveSession(next);
    setSession(next);
  }, []);

  const signOut = useCallback(() => {
    clearSession();
    setSession(null);
  }, []);

  const value = useMemo(() => ({ session, signIn, signOut }), [session, signIn, signOut]);

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}
