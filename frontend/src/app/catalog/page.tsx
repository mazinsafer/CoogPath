"use client";

import { useState, useEffect, useMemo } from "react";
import Link from "next/link";

import { apiUrl } from "@/lib/api";

interface Course {
  courseId: number;
  subject: string;
  number: string;
  title: string;
  credits: number;
}

export default function CatalogPage() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [search, setSearch] = useState("");
  const [activeSubject, setActiveSubject] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch(apiUrl("/courses"))
      .then((r) => r.json())
      .then((data: Course[]) => { setCourses(data); setLoading(false); })
      .catch(() => setLoading(false));
  }, []);

  const subjects = useMemo(() => {
    const map = new Map<string, Course[]>();
    for (const c of courses) {
      const list = map.get(c.subject) || [];
      list.push(c);
      map.set(c.subject, list);
    }
    return Array.from(map.entries()).sort(([a], [b]) => a.localeCompare(b));
  }, [courses]);

  const filtered = useMemo(() => {
    let list = activeSubject ? subjects.filter(([s]) => s === activeSubject) : subjects;
    if (search) {
      const q = search.toLowerCase();
      list = list.map(([subj, crses]) => [subj, crses.filter(
        (c) => `${c.subject} ${c.number}`.toLowerCase().includes(q) || c.title.toLowerCase().includes(q)
      )] as [string, Course[]]).filter(([, crses]) => crses.length > 0);
    }
    return list;
  }, [subjects, search, activeSubject]);

  return (
    <div className="min-h-screen bg-[#0a0a0a]">
      <nav className="border-b border-[#1a1a1a] px-8 py-4 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link href="/dashboard" className="text-zinc-500 hover:text-white text-sm">&larr; Back to Roadmap</Link>
          <h1 className="text-lg font-bold">Course Catalog</h1>
        </div>
        <span className="text-sm text-zinc-500">{courses.length} courses</span>
      </nav>

      <div className="max-w-5xl mx-auto px-8 py-6">
        <div className="flex items-center gap-3 mb-6">
          <div className="flex-1 flex items-center bg-[#141414] border border-[#222] rounded-lg px-4 py-2.5">
            <svg className="w-4 h-4 text-zinc-500 mr-2.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input type="text" value={search} onChange={(e) => setSearch(e.target.value)}
              placeholder="Search courses..." className="w-full bg-transparent text-sm focus:outline-none" />
          </div>
        </div>

        <div className="flex flex-wrap gap-2 mb-6">
          <button onClick={() => setActiveSubject(null)}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${!activeSubject ? "bg-[#c8102e] text-white" : "bg-[#141414] border border-[#222] text-zinc-400"}`}>
            All
          </button>
          {subjects.map(([subj]) => (
            <button key={subj} onClick={() => setActiveSubject(activeSubject === subj ? null : subj)}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${activeSubject === subj ? "bg-[#c8102e] text-white" : "bg-[#141414] border border-[#222] text-zinc-400"}`}>
              {subj}
            </button>
          ))}
        </div>

        {loading ? (
          <div className="text-center text-zinc-500 py-20">Loading...</div>
        ) : (
          <div className="space-y-8">
            {filtered.map(([subject, subjectCourses]) => (
              <div key={subject}>
                <div className="flex items-center gap-3 mb-3">
                  <h2 className="text-sm font-semibold text-zinc-300">{subject}</h2>
                  <div className="flex-1 h-px bg-[#1a1a1a]" />
                </div>
                <div className="bg-[#141414] border border-[#1e1e1e] rounded-xl overflow-hidden">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="text-left text-zinc-500 text-xs uppercase tracking-wider border-b border-[#1e1e1e]">
                        <th className="px-4 py-3">Code</th>
                        <th className="px-4 py-3">Title</th>
                        <th className="px-4 py-3 text-center">Credits</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#111]">
                      {subjectCourses.map((c) => (
                        <tr key={c.courseId} className="hover:bg-[#1a1a1a]">
                          <td className="px-4 py-3 font-medium text-zinc-300">{c.subject} {c.number}</td>
                          <td className="px-4 py-3 text-zinc-500">{c.title}</td>
                          <td className="px-4 py-3 text-center text-zinc-500">{c.credits}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
