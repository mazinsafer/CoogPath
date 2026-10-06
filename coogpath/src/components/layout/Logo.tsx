import { Link } from "react-router-dom";

export function LogoMark({ className = "size-7" }: { className?: string }) {
  return (
    <svg viewBox="0 0 32 32" className={className} aria-hidden="true">
      <rect width="32" height="32" rx="7" fill="#C8102E" />
      <path
        d="M9 21.5V10.5h7.2a4.3 4.3 0 0 1 0 8.6H12.6"
        fill="none"
        stroke="#fff"
        strokeWidth="2.6"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

export function Logo({ to = "/" }: { to?: string }) {
  return (
    <Link to={to} className="flex items-center gap-2.5 rounded-md">
      <LogoMark />
      <span className="text-[15px] font-semibold tracking-tight text-zinc-900">CoogPath</span>
    </Link>
  );
}
