import { NavLink, useNavigate } from "react-router-dom";
import { useSession } from "../../hooks/useSession";
import { initials } from "../../lib/format";
import { IconBook, IconChecklist, IconLogout, IconRoadmap, IconSliders, IconTranscript, IconX } from "../Icons";
import { Logo } from "./Logo";

const NAV = [
  {
    section: "Plan",
    items: [
      { to: "/roadmap", label: "Roadmap", icon: IconRoadmap },
      { to: "/requirements", label: "Requirements", icon: IconChecklist },
    ],
  },
  {
    section: "Courses",
    items: [
      { to: "/transcript", label: "Transcript", icon: IconTranscript },
      { to: "/catalog", label: "Catalog", icon: IconBook },
      { to: "/setup", label: "Courses & options", icon: IconSliders },
    ],
  },
];

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

export function Sidebar({ open, onClose }: SidebarProps) {
  const { session, signOut } = useSession();
  const navigate = useNavigate();
  const name = session?.name ?? "";

  const handleSignOut = () => {
    navigate("/");
    signOut();
  };

  return (
    <>
      {open && <div className="fixed inset-0 z-30 bg-zinc-900/30 lg:hidden" onClick={onClose} aria-hidden="true" />}
      <aside
        className={`fixed inset-y-0 left-0 z-40 flex w-60 flex-col border-r border-zinc-200 bg-white transition-transform lg:sticky lg:top-0 lg:h-screen lg:translate-x-0 ${
          open ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="flex h-14 items-center justify-between px-4">
          <Logo to="/roadmap" />
          <button
            type="button"
            onClick={onClose}
            className="rounded-md p-1.5 text-zinc-500 hover:bg-zinc-100 lg:hidden"
            aria-label="Close navigation"
          >
            <IconX />
          </button>
        </div>

        <nav className="flex-1 space-y-6 overflow-y-auto px-3 pt-4" aria-label="Main">
          {NAV.map((group) => (
            <div key={group.section}>
              <p className="px-2 pb-1.5 text-xs font-medium text-zinc-500">{group.section}</p>
              <ul className="space-y-0.5">
                {group.items.map(({ to, label, icon: Icon }) => (
                  <li key={to}>
                    <NavLink
                      to={to}
                      onClick={onClose}
                      className={({ isActive }) =>
                        `flex items-center gap-2.5 rounded-md px-2 py-1.5 text-sm transition-colors ${
                          isActive
                            ? "bg-zinc-100 font-medium text-zinc-900"
                            : "text-zinc-600 hover:bg-zinc-50 hover:text-zinc-900"
                        }`
                      }
                    >
                      {({ isActive }) => (
                        <>
                          <Icon className={`size-4 ${isActive ? "text-brand-600" : "text-zinc-400"}`} />
                          {label}
                        </>
                      )}
                    </NavLink>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </nav>

        <div className="border-t border-zinc-200 p-3">
          <div className="flex items-center gap-2.5 px-2 py-1.5">
            <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-zinc-100 text-xs font-semibold text-zinc-600">
              {initials(name)}
            </span>
            <span className="min-w-0 flex-1 truncate text-sm font-medium text-zinc-800">{name}</span>
            <button
              type="button"
              onClick={handleSignOut}
              className="rounded-md p-1.5 text-zinc-500 hover:bg-zinc-100 hover:text-zinc-900"
              aria-label="Sign out"
              title="Sign out"
            >
              <IconLogout />
            </button>
          </div>
        </div>
      </aside>
    </>
  );
}
