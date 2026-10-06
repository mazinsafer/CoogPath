import type { ReactNode } from "react";
import { Logo } from "./Logo";
import { SiteFooter } from "./SiteFooter";

interface AuthLayoutProps {
  title: string;
  description: string;
  children: ReactNode;
  footer: ReactNode;
}

export function AuthLayout({ title, description, children, footer }: AuthLayoutProps) {
  return (
    <div className="flex min-h-screen flex-col">
      <div className="flex flex-1 flex-col items-center justify-center px-4 py-12">
        <Logo />
        <div className="mt-8 w-full max-w-sm rounded-lg border border-zinc-200 bg-white p-6 shadow-sm sm:p-8">
          <h1 className="text-lg font-semibold tracking-tight text-zinc-900">{title}</h1>
          <p className="mt-1 text-sm text-zinc-500">{description}</p>
          <div className="mt-6">{children}</div>
        </div>
        <p className="mt-6 text-sm text-zinc-500">{footer}</p>
      </div>
      <SiteFooter />
    </div>
  );
}
