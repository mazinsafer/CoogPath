"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

import { apiUrl } from "@/lib/api";

interface TranscriptEntry {
  courseCode: string;
  title: string;
  credits: number;
  status: string;
  grade: string | null;
}

export default function TranscriptPage() {
  const router = useRouter();
  const [entries, setEntries] = useState<TranscriptEntry[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const id = localStorage.getItem("studentId");
    if (!id) { router.push("/login"); return; }
    fetch(apiUrl(`/students/${id}/transcript`))
      .then((r) => r.json())
      .then((data) => { setEntries(Array.isArray(data) ? data : []); setLoading(false); })
      .catch(() => setLoading(false));
  }, [router]);

  const totalCredits = entries.reduce((s, e) => s + e.credits, 0);

  return (
    <div className="min-h-screen bg-[#0a0a0a]">
      <nav className="border-b border-[#1a1a1a] px-8 py-4 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link href="/dashboard" className="text-zinc-500 hover:text-white text-sm">&larr; Back to Roadmap</Link>
          <h1 className="text-lg font-bold">My Transcript</h1>
        </div>
        <div className="text-sm text-zinc-400">
          {entries.length} courses &middot; {totalCredits} credits
        </div>
      </nav>

      <div className="max-w-3xl mx-auto px-8 py-8">
        {loading ? (
          <div className="text-center text-zinc-500 py-20">Loading transcript...</div>
        ) : entries.length === 0 ? (
          <div className="text-center py-20">
            <div className="text-zinc-500 mb-4">No courses on your transcript yet</div>
            <Link href="/courses" className="text-[#c8102e] text-sm hover:underline">
              Add completed courses &rarr;
            </Link>
          </div>
        ) : (
          <div className="bg-[#141414] border border-[#1e1e1e] rounded-xl overflow-hidden">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-zinc-500 text-xs uppercase tracking-wider border-b border-[#1e1e1e]">
                  <th className="px-6 py-3">Course</th>
                  <th className="px-6 py-3">Title</th>
                  <th className="px-6 py-3 text-center">Credits</th>
                  <th className="px-6 py-3 text-center">Grade</th>
                  <th className="px-6 py-3 text-center">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#111]">
                {entries.map((e) => (
                  <tr key={e.courseCode} className="hover:bg-[#1a1a1a]">
                    <td className="px-6 py-3 font-medium">{e.courseCode}</td>
                    <td className="px-6 py-3 text-zinc-500">{e.title}</td>
                    <td className="px-6 py-3 text-center text-zinc-400">{e.credits}</td>
                    <td className="px-6 py-3 text-center">
                      <span className={`text-xs font-medium ${e.grade === "A" || e.grade === "B" ? "text-[#22c55e]" : e.grade === "F" ? "text-red-400" : "text-zinc-400"}`}>
                        {e.grade ?? "—"}
                      </span>
                    </td>
                    <td className="px-6 py-3 text-center">
                      <span className={`text-[10px] px-2 py-0.5 rounded-full font-medium ${
                        e.status === "TAKEN" ? "bg-[#22c55e]/10 text-[#22c55e]" :
                        e.status === "IN_PROGRESS" ? "bg-[#3b82f6]/10 text-[#3b82f6]" :
                        e.status === "TRANSFER" ? "bg-amber-500/10 text-amber-400" :
                        "bg-zinc-500/10 text-zinc-400"
                      }`}>
                        {e.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
