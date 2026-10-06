import { formatTermLabel } from "../../lib/terms";
import type { PlannedTerm } from "../../types/plan";
import { Badge } from "../ui/Badge";

interface TermCardProps {
  term: PlannedTerm;
  index: number;
}

export function TermCard({ term, index }: TermCardProps) {
  return (
    <section className="overflow-hidden rounded-lg border border-zinc-200 bg-white">
      <header className="flex items-center justify-between gap-3 border-b border-zinc-200 bg-zinc-50/60 px-5 py-3">
        <div className="flex items-center gap-3">
          <span className="flex size-6 items-center justify-center rounded-full border border-zinc-300 bg-white text-xs font-medium text-zinc-600 tabular-nums">
            {index + 1}
          </span>
          <h3 className="text-sm font-semibold text-zinc-900">{formatTermLabel(term.termLabel)}</h3>
          {index === 0 && <Badge tone="info">Next term</Badge>}
        </div>
        <span className="text-sm text-zinc-500 tabular-nums">{term.totalCredits} credits</span>
      </header>
      <table className="w-full text-sm">
        <thead className="sr-only">
          <tr>
            <th>Course</th>
            <th>Title</th>
            <th>Credits</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-zinc-100">
          {term.courses.map((course) => (
            <tr key={course.courseCode}>
              <td className="w-40 py-2.5 pl-5 pr-3 font-medium whitespace-nowrap text-zinc-900">{course.courseCode}</td>
              <td className="py-2.5 pr-3 text-zinc-600">{course.title}</td>
              <td className="w-16 py-2.5 pr-5 text-right text-zinc-500 tabular-nums">{course.credits}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
