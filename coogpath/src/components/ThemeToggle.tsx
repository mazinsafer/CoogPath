import { useTheme } from "../lib/theme";
import { IconMoon, IconSun } from "./Icons";

export function ThemeToggle({ className = "" }: { className?: string }) {
  const [theme, setTheme] = useTheme();
  const isDark = theme === "dark";
  const label = isDark ? "Switch to light mode" : "Switch to dark mode";

  return (
    <button
      type="button"
      onClick={() => setTheme(isDark ? "light" : "dark")}
      className={`rounded-md p-1.5 text-zinc-500 hover:bg-zinc-100 hover:text-zinc-900 ${className}`}
      aria-label={label}
      title={label}
    >
      {isDark ? <IconSun /> : <IconMoon />}
    </button>
  );
}
