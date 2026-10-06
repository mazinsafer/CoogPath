import type { ReactNode } from "react";

type Tone = "neutral" | "success" | "info" | "warning";

const TONES: Record<Tone, string> = {
  neutral: "bg-zinc-100 text-zinc-700 ring-zinc-200",
  success: "bg-emerald-50 text-emerald-700 ring-emerald-200",
  info: "bg-sky-50 text-sky-700 ring-sky-200",
  warning: "bg-amber-50 text-amber-800 ring-amber-200",
};

export function Badge({ tone = "neutral", children }: { tone?: Tone; children: ReactNode }) {
  return (
    <span className={`inline-flex items-center rounded px-1.5 py-0.5 text-xs font-medium ring-1 ring-inset ${TONES[tone]}`}>
      {children}
    </span>
  );
}
