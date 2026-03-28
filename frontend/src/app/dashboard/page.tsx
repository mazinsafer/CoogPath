"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import jsPDF from "jspdf";

import { apiUrl } from "@/lib/api";

interface PlannedCourse {
  courseCode: string;
  title: string;
  credits: number;
  prereqString: string;
  reason: string;
}

interface PlannedTerm {
  termLabel: string;
  season: string;
  year: number;
  totalCredits: number;
  courses: PlannedCourse[];
}

interface PlanResult {
  terms: PlannedTerm[];
  unmetRequirements: string[];
  blockers: string[];
}

const NAV_ITEMS: Record<string, { label: string; icon: () => React.JSX.Element; active?: boolean; href?: string }[]> = {
  PLANNING: [
    { label: "My Roadmap", icon: MapIcon, active: true },
    { label: "Requirements", icon: ClipboardIcon, href: "/requirements" },
  ],
  COURSES: [
    { label: "Course Catalog", icon: BookIcon, href: "/catalog" },
    { label: "My Transcript", icon: FileIcon, href: "/transcript" },
  ],
  TOOLS: [
    { label: "AI Advisor", icon: SparkleIcon },
    { label: "Export PDF", icon: DownloadIcon, href: "EXPORT_PDF" },
  ],
};

const MODE_MAP: Record<string, string> = {
  "Fastest": "fastest",
  "Balanced": "balanced",
};

export default function DashboardPage() {
  const router = useRouter();
  const [plan, setPlan] = useState<PlanResult | null>(null);
  const [loading, setLoading] = useState(true);
  const [studentName, setStudentName] = useState("");
  const [studentId, setStudentId] = useState<string | null>(null);
  const [planMode, setPlanMode] = useState("Fastest");
  const [startSemester, setStartSemester] = useState("");
  const [includeSummer, setIncludeSummer] = useState(false);

  useEffect(() => {
    const id = localStorage.getItem("studentId");
    const name = localStorage.getItem("studentName");
    if (!id) {
      router.push("/login");
      return;
    }
    setStudentId(id);
    setStudentName(name || "Student");

    const savedStart = localStorage.getItem("startSemester");
    const savedSummer = localStorage.getItem("includeSummer") === "true";
    setIncludeSummer(savedSummer);

    if (savedStart) {
      setStartSemester(savedStart);
      fetchPlan(id, "fastest", savedStart, savedSummer);
    } else {
      const now = new Date();
      const m = now.getMonth();
      const y = now.getFullYear();
      const defaultStart = m < 8 ? `FALL-${y}` : `SPRING-${y + 1}`;
      setStartSemester(defaultStart);
      fetchPlan(id, "fastest", defaultStart, savedSummer);
    }
  }, [router]);

  const fetchPlan = async (id: string, mode?: string, start?: string, summer?: boolean) => {
    setLoading(true);
    try {
      const modeKey = MODE_MAP[mode ?? planMode] ?? (mode ?? "fastest");
      const sem = start ?? startSemester;
      const [season, year] = sem.includes("-") ? sem.split("-") : ["FALL", "2026"];
      const useSummer = summer ?? includeSummer;
      const params = new URLSearchParams({
        mode: modeKey,
        startSeason: season,
        startYear: year,
        includeSummer: String(useSummer),
      });
      const res = await fetch(`${apiUrl(`/plan/generate/${id}`)}?${params}`);
      if (!res.ok) throw new Error("Failed to generate plan");
      const data: PlanResult = await res.json();
      setPlan(data);
    } catch {
      setPlan(null);
    } finally {
      setLoading(false);
    }
  };

  const handleModeChange = (mode: string) => {
    setPlanMode(mode);
    if (studentId) fetchPlan(studentId, mode);
  };

  const handleSemesterChange = (sem: string) => {
    setStartSemester(sem);
    localStorage.setItem("startSemester", sem);
    if (studentId) fetchPlan(studentId, planMode, sem);
  };

  const creditsRemaining = plan?.terms.reduce((sum, t) => sum + t.totalCredits, 0) ?? 0;
  const totalCourses = plan?.terms.reduce((sum, t) => sum + t.courses.length, 0) ?? 0;
  const semestersLeft = plan?.terms.length ?? 0;
  const lastTerm = plan?.terms[plan.terms.length - 1];
  const graduationTarget = lastTerm ? lastTerm.termLabel : "—";

  const handleExportPDF = () => {
    if (!plan) return;
    const doc = new jsPDF();
    const pageW = doc.internal.pageSize.getWidth();
    let y = 20;

    doc.setFontSize(20);
    doc.setTextColor(200, 16, 46);
    doc.text("CoogPath Degree Roadmap", pageW / 2, y, { align: "center" });
    y += 10;

    doc.setFontSize(11);
    doc.setTextColor(100);
    doc.text(`Student: ${studentName}`, 14, y);
    doc.text(`Generated: ${new Date().toLocaleDateString()}`, pageW - 14, y, { align: "right" });
    y += 4;

    doc.setDrawColor(200);
    doc.line(14, y, pageW - 14, y);
    y += 8;

    for (const term of plan.terms) {
      if (y > 265) { doc.addPage(); y = 20; }

      doc.setFontSize(13);
      doc.setTextColor(30);
      doc.text(term.termLabel, 14, y);
      doc.setFontSize(10);
      doc.setTextColor(140);
      doc.text(`${term.totalCredits} credits`, pageW - 14, y, { align: "right" });
      y += 6;

      doc.setFontSize(10);
      for (const c of term.courses) {
        if (y > 275) { doc.addPage(); y = 20; }
        doc.setTextColor(60);
        doc.text(`${c.courseCode}`, 18, y);
        doc.setTextColor(110);
        doc.text(c.title, 55, y);
        doc.text(`${c.credits} cr`, pageW - 14, y, { align: "right" });
        y += 5;
      }
      y += 6;
    }

    if (plan.blockers.length > 0) {
      if (y > 260) { doc.addPage(); y = 20; }
      doc.setFontSize(12);
      doc.setTextColor(200, 16, 46);
      doc.text("Blockers", 14, y);
      y += 6;
      doc.setFontSize(10);
      doc.setTextColor(80);
      for (const b of plan.blockers) {
        if (y > 275) { doc.addPage(); y = 20; }
        doc.text(`• ${b}`, 18, y);
        y += 5;
      }
    }

    doc.save(`coogpath-roadmap-${studentName.replace(/\s+/g, "-").toLowerCase()}.pdf`);
  };

  const handleLogout = () => {
    localStorage.clear();
    router.push("/");
  };

  return (
    <div className="flex h-screen bg-[#0a0a0a] overflow-hidden">
      {/* Left Sidebar */}
      <aside className="w-56 bg-[#0f0f0f] border-r border-[#1a1a1a] flex flex-col shrink-0">
        <div className="p-5">
          <Link href="/" className="flex items-center gap-2">
            <div className="w-7 h-7 bg-[#c8102e] rounded-lg flex items-center justify-center text-[10px] font-bold">
              CP
            </div>
            <div>
              <div className="text-sm font-semibold leading-tight">CoogPath</div>
              <div className="text-[10px] text-zinc-600">UH Degree Planner</div>
            </div>
          </Link>
        </div>

        <nav className="flex-1 px-3 space-y-6 mt-2">
          {Object.entries(NAV_ITEMS).map(([section, items]) => (
            <div key={section}>
              <div className="text-[10px] font-semibold text-zinc-600 uppercase tracking-wider px-2 mb-2">
                {section}
              </div>
              <div className="space-y-0.5">
                {items.map((item) => (
                  <button
                    key={item.label}
                    onClick={() => {
                      if (item.href === "EXPORT_PDF") { handleExportPDF(); }
                      else if (item.href) { router.push(item.href); }
                    }}
                    className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm transition-colors ${
                      item.active
                        ? "bg-[#c8102e]/10 text-[#c8102e]"
                        : "text-zinc-500 hover:text-zinc-300 hover:bg-[#141414]"
                    }`}
                  >
                    <item.icon />
                    {item.label}
                  </button>
                ))}
              </div>
            </div>
          ))}
        </nav>

        <div className="p-4 border-t border-[#1a1a1a]">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 bg-[#1a1a1a] rounded-full flex items-center justify-center text-xs font-bold text-zinc-400">
              {studentName.split(" ").map((n) => n[0]).join("")}
            </div>
            <div className="flex-1 min-w-0">
              <div className="text-sm font-medium truncate">{studentName}</div>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="text-xs text-zinc-600 hover:text-zinc-400 mt-3 transition-colors"
          >
            Sign out
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-y-auto">
        {/* Top Bar */}
        <div className="sticky top-0 z-10 bg-[#0a0a0a]/80 backdrop-blur-xl border-b border-[#1a1a1a] px-8 py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-xl font-bold">My Roadmap</h1>
            <div className="flex items-center gap-3">
              <div className="flex bg-[#141414] border border-[#222] rounded-lg p-0.5">
                {["Fastest", "Balanced"].map((mode) => (
                  <button
                    key={mode}
                    onClick={() => handleModeChange(mode)}
                    className={`px-3.5 py-1.5 rounded-md text-xs font-medium transition-colors ${
                      planMode === mode ? "bg-[#c8102e] text-white" : "text-zinc-500 hover:text-zinc-300"
                    }`}
                  >
                    {mode}
                  </button>
                ))}
              </div>

              <select
                value={startSemester}
                onChange={(e) => handleSemesterChange(e.target.value)}
                className="bg-[#141414] border border-[#222] rounded-lg px-3 py-1.5 text-xs text-zinc-300 focus:outline-none focus:border-[#c8102e] transition-colors appearance-none"
              >
                {(() => {
                  const opts: { value: string; label: string }[] = [];
                  const base = new Date().getFullYear();
                  for (let y = base; y <= base + 3; y++) {
                    opts.push({ value: `SPRING-${y}`, label: `Spring ${y}` });
                    opts.push({ value: `SUMMER-${y}`, label: `Summer ${y}` });
                    opts.push({ value: `FALL-${y}`, label: `Fall ${y}` });
                  }
                  return opts.map((o) => <option key={o.value} value={o.value}>{o.label}</option>);
                })()}
              </select>

              <button
                onClick={() => studentId && fetchPlan(studentId)}
                className="flex items-center gap-1.5 bg-[#141414] border border-[#222] rounded-lg px-3 py-1.5 text-xs text-zinc-400 hover:text-white hover:border-[#333] transition-colors"
              >
                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                Recalculate
              </button>

              <button
                onClick={() => router.push("/courses")}
                className="flex items-center gap-1.5 bg-[#c8102e] text-white rounded-lg px-3.5 py-1.5 text-xs font-medium hover:bg-[#a00d24] transition-colors"
              >
                <span className="text-sm">+</span>
                Add Course
              </button>
            </div>
          </div>
        </div>

        {loading ? (
          <div className="flex items-center justify-center h-96">
            <div className="text-zinc-500">Generating your roadmap...</div>
          </div>
        ) : plan ? (
          <div className="px-8 py-6">
            {/* Stat Cards */}
            <div className="grid grid-cols-4 gap-4 mb-6">
              <StatCard
                label="CREDITS REMAINING"
                value={String(creditsRemaining)}
                sub="credits left to graduate"
                color="blue"
                icon="book"
              />
              <StatCard
                label="SEMESTERS LEFT"
                value={String(semestersLeft)}
                sub={`Graduating ${graduationTarget}`}
                color="blue"
                icon="calendar"
              />
              <StatCard
                label="COURSES REMAINING"
                value={String(totalCourses)}
                sub={`${Math.round(totalCourses / Math.max(semestersLeft, 1))} per semester avg`}
                color="red"
                icon="book"
              />
              <StatCard
                label="BOTTLENECKS"
                value={String(plan.blockers.length)}
                sub={plan.blockers.length > 0 ? "courses blocking progress" : "no blockers detected"}
                color={plan.blockers.length > 0 ? "amber" : "green"}
                icon="warning"
              />
            </div>

            {/* Blockers Alert */}
            {plan.blockers.length > 0 && (
              <div className="bg-amber-500/5 border border-amber-500/20 rounded-xl px-5 py-4 mb-6 flex items-start gap-3">
                <svg className="w-5 h-5 text-amber-500 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
                </svg>
                <div>
                  <div className="text-amber-500 font-semibold text-sm">Bottleneck Detected</div>
                  <div className="text-zinc-400 text-sm mt-0.5">{plan.blockers[0]}</div>
                </div>
              </div>
            )}

            {/* Semester Roadmap Header */}
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold">Semester Roadmap</h2>
            </div>

            {/* Semester Timeline */}
            <div className="relative">
              {plan.terms.map((term, termIndex) => (
                <div key={term.termLabel} className="relative pl-8 pb-8">
                  {/* Timeline dot + line */}
                  <div className="absolute left-0 top-0 flex flex-col items-center">
                    <div className={`w-3 h-3 rounded-full border-2 ${
                      termIndex === 0 ? "bg-[#c8102e] border-[#c8102e]" : "bg-[#0a0a0a] border-[#333]"
                    }`} />
                    {termIndex < plan.terms.length - 1 && (
                      <div className="w-px flex-1 bg-[#1a1a1a] mt-1" style={{ minHeight: "100%" }} />
                    )}
                  </div>

                  {/* Term Header */}
                  <div className="flex items-center gap-3 mb-3 -mt-1">
                    <span className="font-semibold text-sm">{term.termLabel}</span>
                    <span className="text-xs text-zinc-600">{term.totalCredits} credits</span>
                    {termIndex === 0 && (
                      <span className="text-[10px] bg-[#3b82f6]/10 text-[#3b82f6] px-2 py-0.5 rounded-full font-medium">
                        Up Next
                      </span>
                    )}
                  </div>

                  {/* Course Cards */}
                  <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
                    {term.courses.map((course) => (
                      <div
                        key={course.courseCode}
                        className="bg-[#141414] border border-[#1e1e1e] rounded-xl p-4 hover:border-[#2a2a2a] transition-colors group"
                      >
                        <div className="flex items-start justify-between mb-2">
                          <span className="text-xs font-semibold text-zinc-300">{course.courseCode}</span>
                        </div>
                        <div className="text-xs text-zinc-500 mb-2 leading-relaxed line-clamp-2">{course.title}</div>
                        <div className="text-[10px] text-zinc-600">{course.credits} credits</div>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <div className="flex items-center justify-center h-96">
            <div className="text-zinc-500">Failed to load plan. Try recalculating.</div>
          </div>
        )}
      </main>

      {/* Right Sidebar */}
      <aside className="w-72 bg-[#0f0f0f] border-l border-[#1a1a1a] overflow-y-auto shrink-0">
        <div className="p-5">
          {/* Plan Summary */}
          <div className="mb-8">
            <h3 className="font-semibold text-sm mb-4">Plan Summary</h3>
            {plan ? (
              <div className="space-y-3">
                <div className="bg-[#141414] border border-[#1e1e1e] rounded-lg p-3">
                  <div className="text-[10px] text-zinc-500 uppercase tracking-wider mb-1">Total Credits in Plan</div>
                  <div className="text-lg font-bold">{creditsRemaining}</div>
                </div>
                <div className="bg-[#141414] border border-[#1e1e1e] rounded-lg p-3">
                  <div className="text-[10px] text-zinc-500 uppercase tracking-wider mb-1">Total Courses</div>
                  <div className="text-lg font-bold">{totalCourses}</div>
                </div>
                <div className="bg-[#141414] border border-[#1e1e1e] rounded-lg p-3">
                  <div className="text-[10px] text-zinc-500 uppercase tracking-wider mb-1">Graduation Target</div>
                  <div className="text-lg font-bold">{graduationTarget}</div>
                </div>

                {plan.unmetRequirements.length > 0 && (
                  <div className="mt-4">
                    <div className="text-xs font-semibold text-amber-500 mb-2">Unmet Requirements</div>
                    <div className="space-y-1.5">
                      {plan.unmetRequirements.map((req, i) => (
                        <div key={i} className="text-xs text-zinc-400 bg-amber-500/5 border border-amber-500/10 rounded-lg px-3 py-2">
                          {req}
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {plan.unmetRequirements.length === 0 && (
                  <div className="bg-[#22c55e]/5 border border-[#22c55e]/20 rounded-lg p-3">
                    <div className="flex items-center gap-2">
                      <svg className="w-4 h-4 text-[#22c55e]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                      </svg>
                      <span className="text-xs text-[#22c55e] font-medium">All requirements covered</span>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <div className="text-xs text-zinc-600">No plan loaded</div>
            )}
          </div>

          {/* AI Advisor */}
          <div className="bg-[#141414] border border-[#1e1e1e] rounded-xl p-4">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <div className="w-5 h-5 bg-[#c8102e]/10 rounded flex items-center justify-center">
                  <svg className="w-3 h-3 text-[#c8102e]" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" />
                  </svg>
                </div>
                <span className="text-xs font-semibold">AI Advisor</span>
              </div>
              <span className="text-[9px] text-zinc-600 bg-[#1a1a1a] px-2 py-0.5 rounded">Powered by GPT</span>
            </div>
            <p className="text-xs text-zinc-400 leading-relaxed">
              {plan ? (
                <>
                  Your plan covers{" "}
                  <span className="text-white font-medium">{totalCourses} courses</span> across{" "}
                  <span className="text-white font-medium">{semestersLeft} semesters</span>,
                  targeting graduation by{" "}
                  <span className="text-white font-medium">{graduationTarget}</span>.
                  {plan.blockers.length > 0 && (
                    <>
                      <br /><br />
                      <span className="text-amber-400 font-medium">Watch out:</span>{" "}
                      {plan.blockers[0]}
                    </>
                  )}
                  {plan.blockers.length === 0 && (
                    <>
                      <br /><br />
                      No bottlenecks detected — you&apos;re on a clear path!
                    </>
                  )}
                </>
              ) : (
                "Generate a plan to see AI-powered recommendations."
              )}
            </p>
          </div>
        </div>
      </aside>
    </div>
  );
}

/* ── Stat Card ── */
function StatCard({ label, value, sub, color, icon }: {
  label: string; value: string; sub: string; color: string; icon: string;
}) {
  const colorMap: Record<string, string> = {
    green: "#22c55e", blue: "#3b82f6", red: "#c8102e", amber: "#f59e0b",
  };
  const c = colorMap[color] ?? "#22c55e";

  return (
    <div className="bg-[#141414] border border-[#1e1e1e] rounded-xl p-4">
      <div className="flex items-center justify-between mb-3">
        <span className="text-[10px] font-semibold text-zinc-500 uppercase tracking-wider">{label}</span>
        <div className="w-5 h-5 rounded-full flex items-center justify-center" style={{ background: `${c}15` }}>
          {icon === "book" && (
            <svg className="w-3 h-3" style={{ color: c }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
            </svg>
          )}
          {icon === "calendar" && (
            <svg className="w-3 h-3" style={{ color: c }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          )}
          {icon === "warning" && (
            <svg className="w-3 h-3" style={{ color: c }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          )}
        </div>
      </div>
      <div className="text-3xl font-bold">{value}</div>
      <div className="text-[11px] text-zinc-600 mt-0.5">{sub}</div>
    </div>
  );
}

/* ── Icons ── */
function MapIcon() {
  return (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 6.75V15m6-6v8.25m.503 3.498l4.875-2.437c.381-.19.622-.58.622-1.006V4.82c0-.836-.88-1.38-1.628-1.006l-3.869 1.934c-.317.159-.69.159-1.006 0L9.503 3.252a1.125 1.125 0 00-1.006 0L3.622 5.689C3.24 5.88 3 6.27 3 6.695V19.18c0 .836.88 1.38 1.628 1.006l3.869-1.934c.317-.159.69-.159 1.006 0l4.994 2.497c.317.158.69.158 1.006 0z" />
    </svg>
  );
}
function ClipboardIcon() {
  return (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h3.75M9 15h3.75M9 18h3.75m3 .75H18a2.25 2.25 0 002.25-2.25V6.108c0-1.135-.845-2.098-1.976-2.192a48.424 48.424 0 00-1.123-.08m-5.801 0c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 00.75-.75 2.25 2.25 0 00-.1-.664m-5.8 0A2.251 2.251 0 0113.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m0 0H4.875c-.621 0-1.125.504-1.125 1.125v11.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125V9.375c0-.621-.504-1.125-1.125-1.125H8.25z" />
    </svg>
  );
}
function BookIcon() {
  return (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" />
    </svg>
  );
}
function FileIcon() {
  return (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
    </svg>
  );
}
function SparkleIcon() {
  return (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09zM18.259 8.715L18 9.75l-.259-1.035a3.375 3.375 0 00-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 002.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 002.455 2.456L21.75 6l-1.036.259a3.375 3.375 0 00-2.455 2.456zM16.894 20.567L16.5 21.75l-.394-1.183a2.25 2.25 0 00-1.423-1.423L13.5 18.75l1.183-.394a2.25 2.25 0 001.423-1.423l.394-1.183.394 1.183a2.25 2.25 0 001.423 1.423l1.183.394-1.183.394a2.25 2.25 0 00-1.423 1.423z" />
    </svg>
  );
}
function DownloadIcon() {
  return (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5M16.5 12L12 16.5m0 0L7.5 12m4.5 4.5V3" />
    </svg>
  );
}
