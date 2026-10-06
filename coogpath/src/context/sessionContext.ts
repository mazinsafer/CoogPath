import { createContext } from "react";
import type { AuthResponse, Session } from "../types/student";

export interface SessionContextValue {
  session: Session | null;
  /** True after the API rejected the stored token, so sign-in can explain why. */
  expired: boolean;
  signIn: (auth: AuthResponse) => void;
  signOut: () => void;
}

export const SessionContext = createContext<SessionContextValue | null>(null);
