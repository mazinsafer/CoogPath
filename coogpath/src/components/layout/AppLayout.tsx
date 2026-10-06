import { useState } from "react";
import { Outlet } from "react-router-dom";
import { IconMenu } from "../Icons";
import { Logo } from "./Logo";
import { Sidebar } from "./Sidebar";

export function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="flex min-h-screen">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex h-14 items-center gap-3 border-b border-zinc-200 bg-white px-4 lg:hidden">
          <button
            type="button"
            onClick={() => setSidebarOpen(true)}
            className="rounded-md p-1.5 text-zinc-600 hover:bg-zinc-100"
            aria-label="Open navigation"
          >
            <IconMenu className="size-5" />
          </button>
          <Logo to="/roadmap" />
        </header>
        <main className="flex-1">
          <div className="mx-auto w-full max-w-6xl px-4 py-8 sm:px-6 lg:px-10">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
