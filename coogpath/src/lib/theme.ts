import { useSyncExternalStore } from "react";

export type Theme = "light" | "dark";

/** Also read by the inline script in index.html, which applies the theme before first paint. */
const THEME_KEY = "coogpath.theme";

const listeners = new Set<() => void>();

function currentTheme(): Theme {
  return document.documentElement.classList.contains("dark") ? "dark" : "light";
}

export function setTheme(theme: Theme): void {
  document.documentElement.classList.toggle("dark", theme === "dark");
  try {
    localStorage.setItem(THEME_KEY, theme);
  } catch {
    // Private browsing can block storage; the choice still applies to this page.
  }
  listeners.forEach((listener) => listener());
}

function subscribe(listener: () => void): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function useTheme(): [Theme, (theme: Theme) => void] {
  const theme = useSyncExternalStore(subscribe, currentTheme, () => "light" as const);
  return [theme, setTheme];
}
