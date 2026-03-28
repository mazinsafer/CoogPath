"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

interface CourseReq {
  courseCode: string;
  title: string;
  credits: number;
  completed: boolean;
}

interface ReqGroup {
  name: string;
  totalCredits: number;
  completedCredits: number;
  courses: CourseReq[];
}

export default function RequirementsPage() {
  const router = useRouter();
  const [groups, setGroups] = useState<ReqGroup[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const id = localStorage.getItem("studentId");
    if (!id) { router.push("/login"); return; }
    fetch(`/api/requirements/${id}`)
      .then((r) => r.json())
      .then((data) => { setGroups(Array.isArray(data) ? data : []); setLoading(false); })
      .catch(() => setLoading(false));
  }, [router]);

  const totalCredits = groups.reduce((s, g) => s + g.totalCredits, 0);
  const completedCredits = groups.reduce((s, g) => s + g.completedCredits, 0);

  return (
    <div className="min-h-screen bg-[#0a0a0a]">
      <nav className="border-b border-[#1a1a1a] px-8 py-4 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link href="/dashboard" className="text-zinc-500 hover:text-white text-sm">&larr; Back to Roadmap</Link>
          <h1 className="text-lg font-bold">Degree Requirements</h1>
        </div>
        <div className="text-sm text-zinc-400">
          <span className="text-white font-semibold">{completedCredits}</span> / {totalCredits} credits completed
        </div>
      </nav>

      {loading ? (
        <div className="text-center text-zinc-500 py-20">Loading requirements...</div>
      ) : (
        <div className="max-w-4xl mx-auto px-8 py-8 space-y-6">
          {/* Overall progress */}
          <div className="bg-[#141414] border border-[#1e1e1e] rounded-xl p-6">
            <div className="flex items-center justify-between mb-3">
              <span className="text-sm font-semibold">Overall Progress</span>
              <span className="text-sm text-zinc-400">{Math.round((completedCredits / Math.max(totalCredits, 1)) * 100)}%</span>
            </div>
            <div className="h-2.5 bg-[#1a1a1a] rounded-full overflow-hidden">
              <div className="h-full bg-[#c8102e] rounded-full transition-all" style={{ width: `${(completedCredits / Math.max(totalCredits, 1)) * 100}%` }} />
            </div>
          </div>

          {groups.map((group) => {
            const pct = group.totalCredits > 0 ? (group.completedCredits / group.totalCredits) * 100 : 0;
            const isComplete = pct >= 100;
            return (
              <div key={group.name} className="bg-[#141414] border border-[#1e1e1e] rounded-xl overflow-hidden">
                <div className="px-6 py-4 border-b border-[#1e1e1e] flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    {isComplete ? (
                      <div className="w-5 h-5 bg-[#22c55e] rounded-full flex items-center justify-center">
                        <svg className="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                        </svg>
                      </div>
                    ) : (
                      <div className="w-5 h-5 rounded-full border-2 border-[#333]" />
                    )}
                    <h2 className="font-semibold text-sm">{group.name}</h2>
                  </div>
                  <span className={`text-xs font-medium ${isComplete ? "text-[#22c55e]" : "text-zinc-500"}`}>
                    {group.completedCredits}/{group.totalCredits} cr
                  </span>
                </div>
                <div className="px-6">
                  <div className="h-1 bg-[#1a1a1a] rounded-full overflow-hidden mt-3">
                    <div className="h-full rounded-full transition-all" style={{ width: `${pct}%`, backgroundColor: isComplete ? "#22c55e" : "#3b82f6" }} />
                  </div>
                </div>
                <div className="px-6 py-3">
                  {group.courses.map((c) => (
                    <div key={c.courseCode} className="flex items-center justify-between py-2 border-b border-[#111] last:border-0">
                      <div className="flex items-center gap-3">
                        {c.completed ? (
                          <div className="w-4 h-4 bg-[#22c55e] rounded flex items-center justify-center">
                            <svg className="w-2.5 h-2.5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                            </svg>
                          </div>
                        ) : (
                          <div className="w-4 h-4 rounded border border-[#333]" />
                        )}
                        <div>
                          <span className="text-sm font-medium">{c.courseCode}</span>
                          <span className="text-xs text-zinc-600 ml-2">{c.title}</span>
                        </div>
                      </div>
                      <span className="text-xs text-zinc-600">{c.credits} cr</span>
                    </div>
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
