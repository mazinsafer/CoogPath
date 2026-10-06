import { Navigate, Route, Routes } from "react-router-dom";
import { AppLayout } from "./components/layout/AppLayout";
import { RequireAuth } from "./components/layout/RequireAuth";
import { CatalogPage } from "./pages/CatalogPage";
import { LandingPage } from "./pages/LandingPage";
import { LoginPage } from "./pages/LoginPage";
import { NotFoundPage } from "./pages/NotFoundPage";
import { RequirementsPage } from "./pages/RequirementsPage";
import { RoadmapPage } from "./pages/RoadmapPage";
import { SetupPage } from "./pages/SetupPage";
import { SignupPage } from "./pages/SignupPage";
import { TranscriptPage } from "./pages/TranscriptPage";

export function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />

      <Route
        element={
          <RequireAuth>
            <AppLayout />
          </RequireAuth>
        }
      >
        <Route path="/roadmap" element={<RoadmapPage />} />
        <Route path="/requirements" element={<RequirementsPage />} />
        <Route path="/transcript" element={<TranscriptPage />} />
        <Route path="/catalog" element={<CatalogPage />} />
        <Route path="/setup" element={<SetupPage />} />
      </Route>

      {/* Paths from the previous Next.js app, kept so old bookmarks still work. */}
      <Route path="/dashboard" element={<Navigate to="/roadmap" replace />} />
      <Route path="/courses" element={<Navigate to="/setup" replace />} />

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
