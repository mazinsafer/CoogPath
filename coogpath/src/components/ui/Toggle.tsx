import { useId, type ReactNode } from "react";

interface ToggleProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
  label: ReactNode;
  description?: ReactNode;
  disabled?: boolean;
}

export function Toggle({ checked, onChange, label, description, disabled }: ToggleProps) {
  const id = useId();
  return (
    <div className="flex items-start gap-3">
      <button
        id={id}
        type="button"
        role="switch"
        aria-checked={checked}
        disabled={disabled}
        onClick={() => onChange(!checked)}
        className={`relative mt-0.5 inline-flex h-5 w-9 shrink-0 items-center rounded-full transition-colors disabled:opacity-50 ${
          checked ? "bg-brand-600" : "bg-zinc-300"
        }`}
      >
        <span
          className={`inline-block size-4 rounded-full bg-white shadow transition-transform ${
            checked ? "translate-x-4.5" : "translate-x-0.5"
          }`}
        />
      </button>
      <label htmlFor={id} className="cursor-pointer select-none">
        <span className="block text-sm font-medium text-zinc-800">{label}</span>
        {description && <span className="mt-0.5 block text-sm text-zinc-500">{description}</span>}
      </label>
    </div>
  );
}
