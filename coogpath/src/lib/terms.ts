import type { Season, Term } from "../types/plan";

const SEASONS: Season[] = ["SPRING", "SUMMER", "FALL"];

const SEASON_LABELS: Record<Season, string> = {
  SPRING: "Spring",
  SUMMER: "Summer",
  FALL: "Fall",
};

/** "FALL-2026" is the format used for select values and localStorage. */
export function toTermCode(term: Term): string {
  return `${term.season}-${term.year}`;
}

export function parseTermCode(code: string): Term | null {
  const [season, year] = code.split("-");
  const parsedYear = Number(year);
  if (!SEASONS.includes(season as Season) || !Number.isInteger(parsedYear)) return null;
  return { season: season as Season, year: parsedYear };
}

export function formatTerm(term: Term): string {
  return `${SEASON_LABELS[term.season]} ${term.year}`;
}

/** Turns the API's "FALL 2026" into "Fall 2026". */
export function formatTermLabel(label: string): string {
  const [season, year] = label.split(" ");
  return SEASON_LABELS[season as Season] ? `${SEASON_LABELS[season as Season]} ${year}` : label;
}

/** Jan-Aug defaults to the coming fall; Sep-Dec to next spring. Mirrors the API default. */
export function defaultStartTerm(today = new Date()): Term {
  const year = today.getFullYear();
  return today.getMonth() < 8 ? { season: "FALL", year } : { season: "SPRING", year: year + 1 };
}

function currentTerm(today: Date): Term {
  const month = today.getMonth();
  const season: Season = month < 5 ? "SPRING" : month < 7 ? "SUMMER" : "FALL";
  return { season, year: today.getFullYear() };
}

function nextTerm(term: Term): Term {
  if (term.season === "SPRING") return { season: "SUMMER", year: term.year };
  if (term.season === "SUMMER") return { season: "FALL", year: term.year };
  return { season: "SPRING", year: term.year + 1 };
}

/** The current term and the following `count - 1` terms, in calendar order. */
export function startTermOptions(today = new Date(), count = 12): Term[] {
  const terms: Term[] = [currentTerm(today)];
  while (terms.length < count) terms.push(nextTerm(terms[terms.length - 1]!));
  return terms;
}

export function sameTerm(a: Term, b: Term): boolean {
  return a.season === b.season && a.year === b.year;
}
