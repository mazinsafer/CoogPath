import { IconSearch } from "../Icons";

interface CourseFiltersProps {
  query: string;
  onQueryChange: (query: string) => void;
  subject: string | null;
  onSubjectChange: (subject: string | null) => void;
  subjects: { name: string; count: number }[];
  total: number;
}

const CHIP = "h-7 rounded-md border px-2.5 text-xs font-medium transition-colors";
const CHIP_ON = "border-zinc-900 bg-zinc-900 text-white";
const CHIP_OFF = "border-zinc-300 bg-white text-zinc-700 hover:border-zinc-400";

export function CourseFilters({ query, onQueryChange, subject, onSubjectChange, subjects, total }: CourseFiltersProps) {
  return (
    <div className="space-y-3">
      <label className="relative block">
        <span className="sr-only">Search courses</span>
        <IconSearch className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-zinc-400" />
        <input
          type="search"
          value={query}
          onChange={(e) => onQueryChange(e.target.value)}
          placeholder="Search by course code or title"
          className="block h-10 w-full rounded-md border border-zinc-300 bg-white pr-3 pl-9 text-sm shadow-sm placeholder:text-zinc-400 focus:border-brand-600 focus:ring-2 focus:ring-brand-600/15 focus:outline-none"
        />
      </label>
      <div className="flex flex-wrap gap-1.5" role="group" aria-label="Filter by subject">
        <button
          type="button"
          aria-pressed={subject === null}
          onClick={() => onSubjectChange(null)}
          className={`${CHIP} ${subject === null ? CHIP_ON : CHIP_OFF}`}
        >
          All <span className="opacity-60">{total}</span>
        </button>
        {subjects.map(({ name, count }) => (
          <button
            key={name}
            type="button"
            aria-pressed={subject === name}
            onClick={() => onSubjectChange(subject === name ? null : name)}
            className={`${CHIP} ${subject === name ? CHIP_ON : CHIP_OFF}`}
          >
            {name} <span className="opacity-60">{count}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
