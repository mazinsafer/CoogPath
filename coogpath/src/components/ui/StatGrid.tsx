import type { ReactNode } from "react";

export interface Stat {
  label: string;
  value: ReactNode;
  detail?: ReactNode;
}

export function StatGrid({ stats }: { stats: Stat[] }) {
  return (
    <dl className="grid grid-cols-2 divide-zinc-200 overflow-hidden rounded-lg border border-zinc-200 bg-surface lg:grid-cols-4 lg:divide-x">
      {stats.map((stat, i) => (
        <div
          key={stat.label}
          className={`px-5 py-4 ${i % 2 === 1 ? "border-l border-zinc-200 lg:border-l-0" : ""} ${
            i >= 2 ? "border-t border-zinc-200 lg:border-t-0" : ""
          }`}
        >
          <dt className="text-xs font-medium text-zinc-500">{stat.label}</dt>
          <dd className="mt-1 text-2xl font-semibold tracking-tight text-zinc-900 tabular-nums">{stat.value}</dd>
          {stat.detail && <dd className="mt-0.5 text-xs text-zinc-500">{stat.detail}</dd>}
        </div>
      ))}
    </dl>
  );
}
