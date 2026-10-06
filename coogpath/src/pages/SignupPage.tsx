import { useState, type FormEvent } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { AuthLayout } from "../components/layout/AuthLayout";
import { Alert } from "../components/ui/Alert";
import { Button } from "../components/ui/Button";
import { SelectField, TextField } from "../components/ui/Field";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { useSession } from "../hooks/useSession";
import { usePrograms } from "../hooks/useStudentData";
import { register } from "../services/studentService";

const CATALOG_YEAR = 2024;
const MIN_PASSWORD_LENGTH = 8;

function isUhEmail(email: string): boolean {
  const lower = email.trim().toLowerCase();
  return lower.endsWith("@uh.edu") || lower.endsWith("@cougarnet.uh.edu");
}

export function SignupPage() {
  useDocumentTitle("Create account");
  const { session, signIn } = useSession();
  const navigate = useNavigate();
  const programs = usePrograms();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [programId, setProgramId] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [touched, setTouched] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (session) return <Navigate to="/roadmap" replace />;

  const emailError = touched && email && !isUhEmail(email) ? "Use your @uh.edu or @cougarnet.uh.edu address." : null;
  const passwordError =
    touched && password && password.length < MIN_PASSWORD_LENGTH
      ? `Use at least ${MIN_PASSWORD_LENGTH} characters.`
      : null;
  const canSubmit = name.trim() && isUhEmail(email) && password.length >= MIN_PASSWORD_LENGTH && programId;

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setTouched(true);
    if (!canSubmit) return;

    setSubmitting(true);
    setError(null);
    try {
      const auth = await register({
        name: name.trim(),
        email: email.trim(),
        password,
        programId: Number(programId),
        catalogYear: CATALOG_YEAR,
      });
      signIn(auth);
      navigate("/setup", { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Couldn't create your account.");
      setSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title="Create your account"
      description="It takes about a minute. Next, you'll mark the courses you've completed."
      footer={
        <>
          Already have an account?{" "}
          <Link to="/login" className="font-medium text-brand-700 hover:underline">
            Sign in
          </Link>
        </>
      }
    >
      <form onSubmit={handleSubmit} onBlur={() => setTouched(true)} className="space-y-4" noValidate>
        {error && <Alert tone="error">{error}</Alert>}
        {programs.error && (
          <Alert tone="error" title="Majors couldn't be loaded">
            {programs.error}
          </Alert>
        )}
        <TextField
          label="Full name"
          autoComplete="name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <TextField
          label="UH email"
          type="email"
          autoComplete="email"
          placeholder="you@cougarnet.uh.edu"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          error={emailError}
          required
        />
        <TextField
          label="Password"
          type="password"
          autoComplete="new-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          hint={`At least ${MIN_PASSWORD_LENGTH} characters.`}
          error={passwordError}
          required
        />
        <SelectField
          label="Major"
          value={programId}
          onChange={(e) => setProgramId(e.target.value)}
          disabled={programs.loading || !programs.data?.length}
          required
        >
          <option value="">{programs.loading ? "Loading majors…" : "Select your major"}</option>
          {programs.data?.map((program) => (
            <option key={program.programId} value={program.programId}>
              {program.name}
            </option>
          ))}
        </SelectField>
        <Button type="submit" className="w-full" loading={submitting} disabled={!canSubmit}>
          Create account
        </Button>
      </form>
    </AuthLayout>
  );
}
