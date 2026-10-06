import { useContext } from "react";
import { SessionContext, type SessionContextValue } from "../context/sessionContext";
import type { Session } from "../types/student";

export function useSession(): SessionContextValue {
  const value = useContext(SessionContext);
  if (!value) throw new Error("useSession must be used inside <SessionProvider>");
  return value;
}

/** For pages behind <RequireAuth>, where a session is guaranteed. */
export function useRequiredSession(): Session {
  const { session } = useSession();
  if (!session) throw new Error("useRequiredSession used outside <RequireAuth>");
  return session;
}
