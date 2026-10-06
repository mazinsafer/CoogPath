import type { ReactNode } from "react";
import { Button } from "./Button";
import { Spinner } from "./Spinner";
import { IconCircleX } from "../Icons";

export function LoadingState({ label = "Loading…" }: { label?: string }) {
  return (
    <div className="flex items-center justify-center gap-3 py-20 text-sm text-zinc-500" role="status">
      <Spinner className="size-5 text-zinc-400" />
      {label}
    </div>
  );
}

interface EmptyStateProps {
  title: string;
  description?: ReactNode;
  action?: ReactNode;
}

export function EmptyState({ title, description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center px-6 py-16 text-center">
      <p className="text-sm font-medium text-zinc-900">{title}</p>
      {description && <p className="mt-1 max-w-sm text-sm text-zinc-500">{description}</p>}
      {action && <div className="mt-5">{action}</div>}
    </div>
  );
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="flex flex-col items-center px-6 py-16 text-center" role="alert">
      <IconCircleX className="size-6 text-brand-600" />
      <p className="mt-3 text-sm font-medium text-zinc-900">Couldn't load this page</p>
      <p className="mt-1 max-w-sm text-sm text-zinc-500">{message}</p>
      {onRetry && (
        <Button variant="secondary" size="sm" className="mt-5" onClick={onRetry}>
          Try again
        </Button>
      )}
    </div>
  );
}
