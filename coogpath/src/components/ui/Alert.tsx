import type { ReactNode } from "react";
import { IconAlertTriangle, IconCircleX, IconInfo } from "../Icons";

type Tone = "error" | "warning" | "info";

const STYLES: Record<Tone, { box: string; icon: string }> = {
  error: { box: "border-brand-200 bg-brand-50 text-brand-800", icon: "text-brand-600" },
  warning: { box: "border-amber-200 bg-amber-50 text-amber-900", icon: "text-amber-600" },
  info: { box: "border-sky-200 bg-sky-50 text-sky-900", icon: "text-sky-600" },
};

const ICONS = { error: IconCircleX, warning: IconAlertTriangle, info: IconInfo };

interface AlertProps {
  tone?: Tone;
  title?: ReactNode;
  children?: ReactNode;
  action?: ReactNode;
}

export function Alert({ tone = "info", title, children, action }: AlertProps) {
  const Icon = ICONS[tone];
  return (
    <div role={tone === "error" ? "alert" : "status"} className={`flex gap-3 rounded-md border px-4 py-3 text-sm ${STYLES[tone].box}`}>
      <Icon className={`mt-0.5 size-4 shrink-0 ${STYLES[tone].icon}`} />
      <div className="min-w-0 flex-1">
        {title && <p className="font-medium">{title}</p>}
        {children && <div className={title ? "mt-1 opacity-90" : ""}>{children}</div>}
      </div>
      {action && <div className="shrink-0">{action}</div>}
    </div>
  );
}
