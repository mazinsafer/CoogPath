import { LogoMark } from "../components/layout/Logo";
import { ButtonLink } from "../components/ui/Button";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { useSession } from "../hooks/useSession";

export function NotFoundPage() {
  useDocumentTitle("Page not found");
  const { session } = useSession();

  return (
    <main className="flex min-h-screen flex-col items-center justify-center px-6 text-center">
      <LogoMark className="size-9" />
      <p className="mt-6 text-sm font-medium text-brand-700">404</p>
      <h1 className="mt-1 text-xl font-semibold text-zinc-900">Page not found</h1>
      <p className="mt-2 max-w-sm text-sm text-zinc-500">The page you're looking for doesn't exist or has moved.</p>
      <ButtonLink to={session ? "/roadmap" : "/"} className="mt-6">
        {session ? "Back to your roadmap" : "Back to home"}
      </ButtonLink>
    </main>
  );
}
