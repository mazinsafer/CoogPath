import { useState, type FormEvent } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { AuthLayout } from "../components/layout/AuthLayout";
import { Alert } from "../components/ui/Alert";
import { Button } from "../components/ui/Button";
import { TextField } from "../components/ui/Field";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { useSession } from "../hooks/useSession";
import { login } from "../services/authService";

export function LoginPage() {
  useDocumentTitle("Sign in");
  const { session, signIn } = useSession();
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo = (location.state as { from?: string } | null)?.from ?? "/roadmap";

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (session) return <Navigate to="/roadmap" replace />;

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      signIn(await login(email, password));
      navigate(redirectTo, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Sign in failed.");
      setSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title="Sign in"
      description="Welcome back. Pick up where you left off."
      footer={
        <>
          New to CoogPath?{" "}
          <Link to="/signup" className="font-medium text-brand-700 hover:underline">
            Create an account
          </Link>
        </>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4" noValidate>
        {error && <Alert tone="error">{error}</Alert>}
        <TextField
          label="UH email"
          type="email"
          autoComplete="email"
          placeholder="you@cougarnet.uh.edu"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <TextField
          label="Password"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <Button type="submit" className="w-full" loading={submitting} disabled={!email || !password}>
          Sign in
        </Button>
      </form>
    </AuthLayout>
  );
}
