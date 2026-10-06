import { createContext } from "react";
import type { Session, StudentProfile } from "../types/student";

export interface SessionContextValue {
  session: Session | null;
  signIn: (profile: StudentProfile) => void;
  signOut: () => void;
}

export const SessionContext = createContext<SessionContextValue | null>(null);
