import { useId, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes } from "react";

const CONTROL =
  "block w-full rounded-md border border-zinc-300 bg-white px-3 text-sm text-zinc-900 shadow-sm placeholder:text-zinc-400 focus:border-brand-600 focus:ring-2 focus:ring-brand-600/15 focus:outline-none disabled:cursor-not-allowed disabled:bg-zinc-50 disabled:text-zinc-500";

interface FieldShellProps {
  id: string;
  label: string;
  hint?: ReactNode;
  error?: string | null;
  children: ReactNode;
}

function FieldShell({ id, label, hint, error, children }: FieldShellProps) {
  return (
    <div>
      <label htmlFor={id} className="mb-1.5 block text-sm font-medium text-zinc-800">
        {label}
      </label>
      {children}
      {error ? (
        <p className="mt-1.5 text-xs text-brand-700">{error}</p>
      ) : (
        hint && <p className="mt-1.5 text-xs text-zinc-500">{hint}</p>
      )}
    </div>
  );
}

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  hint?: ReactNode;
  error?: string | null;
}

export function TextField({ label, hint, error, className = "", ...rest }: TextFieldProps) {
  const id = useId();
  return (
    <FieldShell id={id} label={label} hint={hint} error={error}>
      <input id={id} className={`${CONTROL} h-10 ${className}`} aria-invalid={error ? true : undefined} {...rest} />
    </FieldShell>
  );
}

interface SelectFieldProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string;
  hint?: ReactNode;
  error?: string | null;
}

export function SelectField({ label, hint, error, className = "", children, ...rest }: SelectFieldProps) {
  const id = useId();
  return (
    <FieldShell id={id} label={label} hint={hint} error={error}>
      <select id={id} className={`${CONTROL} h-10 pr-8 ${className}`} {...rest}>
        {children}
      </select>
    </FieldShell>
  );
}

/** Compact select without a visible label, for toolbars. */
export function InlineSelect({ className = "", ...rest }: SelectHTMLAttributes<HTMLSelectElement>) {
  return <select className={`${CONTROL} h-8 w-auto pr-8 ${className}`} {...rest} />;
}
