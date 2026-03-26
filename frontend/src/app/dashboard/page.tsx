"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

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

const NAV_ITEMS: Record<string, { label: string; icon: () => React.JSX.Element; active?: boolean }[]> = {
  PLANNING: [
    { label: "My Roadmap", icon: MapIcon, active: true },
    { label: "Requirements", icon: ClipboardIcon },
    { label: "What-If Planner", icon: LightbulbIcon },
  ],
  COURSES: [
    { label: "Course Catalog", icon: BookIcon },
    { label: "My Transcript", icon: FileIcon },
  ],
  TOOLS: [
    { label: "AI Advisor", icon: SparkleIcon },
    { label: "Export PDF", icon: DownloadIcon },
  ],
};

export default function DashboardPage() {
  const router = useRouter();
  const [plan, setPlan] = useState<PlanResult | null>(null);
  const [loading, setLoading] = useState(true);
  const [studentName, setStudentName] = useState("");
  const [studentId, setStudentId] = useState<string | null>(null);
  const [planMode, setPlanMode] = useState("Fastest");

  useEffect(() => {
    const id = localStorage.getItem("studentId");
    const name = localStorage.getItem("studentName");
    if (!id) {
      router.push("/login");
      return;
    }
    setStudentId(id);
    setStudentName(name || "Student");
    fetchPlan(id);
  }, [router]);

  const fetchPlan = async (id: string) => {
    setLoading(true);
    try {
      const res = await fetch(`/api/plan/generate/${id}`);
      if (!res.ok) throw new Error("Failed to generate plan");
      const data: PlanResult = await res.json();
      setPlan(data);
    } catch {
      setPlan(null);
    } finally {
      setLoading(false);
    }
  };

  const totalCreditsPlanned = plan?.terms.reduce((sum, t) => sum + t.totalCredits, 0) ?? 0;
  const totalCourses = plan?.terms.reduce((sum, t) => sum + t.courses.length, 0) ?? 0;
  const semestersLeft = plan?.terms.length ?? 0;
  const lastTerm = plan?.terms[plan.terms.length - 1];
  const graduationTarget = lastTerm ? lastTerm.termLabel : "—";

  const handleLogout = () => {
    localStorage.removeItem("studentId");
    localStorage.removeItem("studentName");
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
              <div className="text-[10px] text-zinc-600">CS &middot; Class of &apos;27</div>
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
              {/* Plan Mode Toggle */}
              <div className="flex bg-[#141414] border border-[#222] rounded-lg p-0.5">
                {["Fastest", "Balanced", "Custom"].map((mode) => (
                  <button
                    key={mode}
                    onClick={() => setPlanMode(mode)}
                    className={`px-3.5 py-1.5 rounded-md text-xs font-medium transition-colors ${
                      planMode === mode ? "bg-[#c8102e] text-white" : "text-zinc-500 hover:text-zinc-300"
                    }`}
                  >
                    {mode}
                  </button>
                ))}
              </div>

              <div className="flex items-center gap-2 bg-[#141414] border border-[#222] rounded-lg px-3 py-1.5">
                <svg className="w-3.5 h-3.5 text-zinc-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                <span className="text-xs text-zinc-400">Target:</span>
                <span className="text-xs font-medium">{graduationTarget}</span>
              </div>

              <button
                onClick={() => studentId && fetchPlan(studentId)}
                className="flex items-center gap-1.5 bg-[#141414] border border-[#222] rounded-lg px-3 py-1.5 text-xs text-zinc-400 hover:text-white hover:border-[#333] transition-colors"
              >
                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                Recalculate
              </button>

              <button className="flex items-center gap-1.5 bg-[#c8102e] text-white rounded-lg px-3.5 py-1.5 text-xs font-medium hover:bg-[#a00d24] transition-colors">
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
              <StatCard label="CREDITS COMPLETED" value="62" sub="of 120 total required" color="green" icon="check" />
              <StatCard label="SEMESTERS LEFT" value={String(semestersLeft)} sub={`Graduating ${graduationTarget}`} color="blue" icon="calendar" />
              <StatCard label="COURSES REMAINING" value={String(totalCourses)} sub={`${Math.round(totalCourses / Math.max(semestersLeft, 1))} per semester avg`} color="red" icon="book" />
              <StatCard label="BOTTLENECKS" value={String(plan.blockers.length)} sub="courses blocking progress" color={plan.blockers.length > 0 ? "amber" : "green"} icon="warning" />
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
              <span className="text-xs text-zinc-600">Catalog Year: 2023-24</span>
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
                    {termIndex < 2 && (
                      <span className="text-[10px] bg-[#22c55e]/10 text-[#22c55e] px-2 py-0.5 rounded-full font-medium">
                        Completed
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
                          {termIndex < 2 && (
                            <div className="w-4 h-4 bg-[#22c55e] rounded-full flex items-center justify-center">
                              <svg className="w-2.5 h-2.5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                              </svg>
                            </div>
                          )}
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
          {/* Requirements */}
          <div className="mb-8">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-semibold text-sm">Requirements</h3>
              <span className="text-xs text-zinc-600">5 groups</span>
            </div>
            <div className="space-y-3">
              {[
                { name: "Core Curriculum", current: 42, total: 42, color: "#22c55e" },
                { name: "CS Foundation", current: 12, total: 12, color: "#22c55e" },
                { name: "Math Foundation", current: 14, total: 14, color: "#22c55e" },
                { name: "Advanced CS", current: 3, total: 24, color: "#3b82f6" },
                { name: "Tech Electives", current: 0, total: 12, color: "#3b82f6" },
                { name: "Capstone", current: 0, total: 6, color: "#3b82f6" },
              ].map((req) => (
                <div key={req.name}>
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs text-zinc-400">{req.name}</span>
                    <span className="text-[10px] text-zinc-600">
                      {req.current}/{req.total} cr
                    </span>
                  </div>
                  <div className="h-1.5 bg-[#1a1a1a] rounded-full overflow-hidden">
                    <div
                      className="h-full rounded-full transition-all"
                      style={{
                        width: `${(req.current / req.total) * 100}%`,
                        backgroundColor: req.color,
                      }}
                    />
                  </div>
                </div>
              ))}
            </div>
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
            <div className="text-[10px] text-[#c8102e] font-semibold uppercase tracking-wider mb-2">Opportunity</div>
            <p className="text-xs text-zinc-400 leading-relaxed">
              {plan ? (
                <>
                  I&apos;ve found your fastest path to graduation is{" "}
                  <span className="text-white font-medium">{graduationTarget}</span> &mdash;{" "}
                  {semestersLeft} semesters away.
                  {plan.blockers.length > 0 && (
                    <>
                      <br /><br />
                      <span className="text-white font-medium">{plan.blockers[0]?.split(" ")[0]}</span> is your biggest
                      bottleneck right now. Consider addressing it early to stay on track.
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
        {icon === "check" && (
          <div className="w-5 h-5 rounded-full flex items-center justify-center" style={{ background: `${c}15` }}>
            <svg className="w-3 h-3" style={{ color: c }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
            </svg>
          </div>
        )}
        {icon === "calendar" && (
          <div className="w-5 h-5 rounded-full flex items-center justify-center bg-[#1a1a1a]">
            <svg className="w-3 h-3 text-zinc-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
        )}
        {icon === "book" && (
          <div className="w-5 h-5 rounded-full flex items-center justify-center" style={{ background: `${c}15` }}>
            <svg className="w-3 h-3" style={{ color: c }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
            </svg>
          </div>
        )}
        {icon === "warning" && (
          <div className="w-5 h-5 rounded-full flex items-center justify-center" style={{ background: `${c}15` }}>
            <svg className="w-3 h-3" style={{ color: c }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
        )}
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
function LightbulbIcon() {
  return (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 18v-5.25m0 0a6.01 6.01 0 001.5-.189m-1.5.189a6.01 6.01 0 01-1.5-.189m3.75 7.478a12.06 12.06 0 01-4.5 0m3.75 2.383a14.406 14.406 0 01-3 0M14.25 18v-.192c0-.983.658-1.823 1.508-2.316a7.5 7.5 0 10-7.517 0c.85.493 1.509 1.333 1.509 2.316V18" />
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
