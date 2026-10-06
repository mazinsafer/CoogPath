import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useSession } from "../../hooks/useSession";

export function RequireAuth({ children }: { children: ReactNode }) {
  const { session } = useSession();
  const location = useLocation();
  if (!session) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  return children;
}
