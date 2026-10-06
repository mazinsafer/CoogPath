import { useCallback, useEffect, useMemo, useState, type ReactNode } from "react";
import { setSessionRejectedHandler } from "../services/api";
import { clearSession, loadSession, saveSession } from "../services/sessionStore";
import type { AuthResponse, Session } from "../types/student";
import { SessionContext } from "./sessionContext";

export function SessionProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(() => loadSession());
  const [expired, setExpired] = useState(false);

  const signIn = useCallback((auth: AuthResponse) => {
    const next = {
      studentId: auth.student.studentId,
      name: auth.student.name,
      token: auth.token,
      expiresAt: auth.expiresAt,
    };
    saveSession(next);
    setSession(next);
    setExpired(false);
  }, []);

  const signOut = useCallback(() => {
    clearSession();
    setSession(null);
  }, []);

  useEffect(() => {
    setSessionRejectedHandler(() => {
      clearSession();
      setSession(null);
      setExpired(true);
    });
    return () => setSessionRejectedHandler(() => undefined);
  }, []);

  const value = useMemo(() => ({ session, expired, signIn, signOut }), [session, expired, signIn, signOut]);

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}
