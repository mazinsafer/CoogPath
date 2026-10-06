export function ProgressBar({ value, label, tone = "brand" }: { value: number; label: string; tone?: "brand" | "success" }) {
  const clamped = Math.max(0, Math.min(100, value));
  return (
    <div
      role="progressbar"
      aria-label={label}
      aria-valuenow={clamped}
      aria-valuemin={0}
      aria-valuemax={100}
      className="h-1.5 w-full overflow-hidden rounded-full bg-zinc-200"
    >
      <div
        className={`h-full rounded-full transition-[width] duration-500 ${tone === "success" ? "bg-emerald-600" : "bg-brand-600"}`}
        style={{ width: `${clamped}%` }}
      />
    </div>
  );
}
