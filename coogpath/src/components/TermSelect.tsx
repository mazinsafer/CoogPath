import { useMemo, type ChangeEvent } from "react";
import { formatTerm, parseTermCode, sameTerm, startTermOptions, toTermCode } from "../lib/terms";
import type { Term } from "../types/plan";
import { InlineSelect, SelectField } from "./ui/Field";

interface TermSelectProps {
  value: Term;
  onChange: (term: Term) => void;
  /** Renders a labelled form field instead of a compact toolbar select. */
  label?: string;
  hint?: string;
}

export function TermSelect({ value, onChange, label, hint }: TermSelectProps) {
  const options = useMemo(() => {
    const terms = startTermOptions();
    // Keep a previously saved term selectable even after it has passed.
    return terms.some((t) => sameTerm(t, value)) ? terms : [value, ...terms];
  }, [value]);

  const select = {
    value: toTermCode(value),
    onChange: (e: ChangeEvent<HTMLSelectElement>) => {
      const term = parseTermCode(e.target.value);
      if (term) onChange(term);
    },
    children: options.map((term) => (
      <option key={toTermCode(term)} value={toTermCode(term)}>
        {formatTerm(term)}
      </option>
    )),
  };

  return label ? (
    <SelectField label={label} hint={hint} {...select} />
  ) : (
    <InlineSelect aria-label="Start term" {...select} />
  );
}
