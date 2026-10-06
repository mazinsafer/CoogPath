import type { ProgramOption } from "../../lib/programs";

interface OptionGroupProps<T extends string> {
  name: string;
  label: string;
  options: ProgramOption<T>[];
  value: T;
  onChange: (value: T) => void;
}

/** Radio group rendered as selectable cards. */
export function OptionGroup<T extends string>({ name, label, options, value, onChange }: OptionGroupProps<T>) {
  return (
    <fieldset>
      <legend className="sr-only">{label}</legend>
      <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
        {options.map((option) => {
          const selected = option.value === value;
          return (
            <label
              key={option.value}
              className={`flex cursor-pointer gap-3 rounded-md border p-3.5 transition-colors ${
                selected ? "border-brand-600 bg-brand-50/60 ring-1 ring-brand-600" : "border-zinc-200 hover:border-zinc-300"
              }`}
            >
              <input
                type="radio"
                name={name}
                value={option.value}
                checked={selected}
                onChange={() => onChange(option.value)}
                className="mt-0.5 size-4 shrink-0 accent-brand-600"
              />
              <span>
                <span className="block text-sm font-medium text-zinc-900">{option.label}</span>
                <span className="mt-0.5 block text-xs leading-relaxed text-zinc-500">{option.description}</span>
              </span>
            </label>
          );
        })}
      </div>
    </fieldset>
  );
}
