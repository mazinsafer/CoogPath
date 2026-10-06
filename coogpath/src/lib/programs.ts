import type { CapstoneChoice, FinanceTrack } from "../types/student";

// Must match ProgramRules.java in the API and degree_program rows in the migrations.
export const COMPUTER_SCIENCE_PROGRAM_ID = 1;
export const FINANCE_PROGRAM_ID = 2;

export interface ProgramOption<T extends string> {
  value: T;
  label: string;
  description: string;
}

export const CAPSTONE_OPTIONS: ProgramOption<CapstoneChoice>[] = [
  {
    value: "SENIOR_SE",
    label: "Software Engineering sequence",
    description: "Two-course capstone in software design and development.",
  },
  {
    value: "SENIOR_DS",
    label: "Data Science sequence",
    description: "Two-course capstone in data science and machine learning.",
  },
  {
    value: "MATH_MINOR",
    label: "Math minor",
    description: "18 credits of math, some of which overlap with CS requirements.",
  },
];

export const FINANCE_TRACK_OPTIONS: ProgramOption<FinanceTrack>[] = [
  { value: "STANDARD", label: "Standard", description: "12 credits of upper-division finance electives." },
  {
    value: "RE",
    label: "Real Estate",
    description: "Real estate finance, valuation, development, and asset management.",
  },
  {
    value: "PFP",
    label: "Personal Financial Planning",
    description: "Financial planning, retirement and estate, risk management, and tax.",
  },
  {
    value: "CBC",
    label: "Corporate Banking and Credit",
    description: "Intermediate accounting plus bank management, financial evaluation, and credit analysis.",
  },
  {
    value: "GEM",
    label: "Global Energy Management",
    description: "International finance, energy trading, and energy value chain, plus GEM electives.",
  },
  {
    value: "ECTC",
    label: "Energy Commodities Trading and Consulting",
    description: "Internship or experiential learning plus five ECT&C electives.",
  },
];

export function isComputerScience(programId: number | null | undefined): boolean {
  return programId === COMPUTER_SCIENCE_PROGRAM_ID;
}

export function isFinance(programId: number | null | undefined): boolean {
  return programId === FINANCE_PROGRAM_ID;
}
