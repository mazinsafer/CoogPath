"use client";

import { useState, useEffect, useMemo } from "react";
import { useRouter } from "next/navigation";

import { apiUrl } from "@/lib/api";

interface Course {
  courseId: number;
  subject: string;
  number: string;
  title: string;
  credits: number;
}

const CAPSTONE_OPTIONS = [
  { value: "SENIOR_SE", label: "Software Engineering Sequence", desc: "Two-course capstone in software design and development" },
  { value: "SENIOR_DS", label: "Data Science Sequence", desc: "Two-course capstone in data science and machine learning" },
  { value: "MATH_MINOR", label: "Math Minor", desc: "18 credits of math courses (some overlap with CS requirements)" },
];

export default function CoursesPage() {
  const router = useRouter();
  const [courses, setCourses] = useState<Course[]>([]);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [search, setSearch] = useState("");
  const [activeSubject, setActiveSubject] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [isCSMajor, setIsCSMajor] = useState(false);
  const [capstoneChoice, setCapstoneChoice] = useState("SENIOR_SE");
  const [includeSummer, setIncludeSummer] = useState(false);
  const [startSemester, setStartSemester] = useState("");
  const [freeElectiveCredits, setFreeElectiveCredits] = useState(0);

  useEffect(() => {
    const id = localStorage.getItem("studentId");
    if (!id) {
      router.push("/signup");
      return;
    }
    const programName = localStorage.getItem("programName") || "";
    setIsCSMajor(programName.toLowerCase().includes("computer science"));

    const savedCapstone = localStorage.getItem("capstoneChoice");
    if (savedCapstone) setCapstoneChoice(savedCapstone);

    const savedSummer = localStorage.getItem("includeSummer");
    if (savedSummer) setIncludeSummer(savedSummer === "true");

    const savedStart = localStorage.getItem("startSemester");
    if (savedStart) {
      setStartSemester(savedStart);
    } else {
      const now = new Date();
      const m = now.getMonth();
      const y = now.getFullYear();
      if (m < 5) setStartSemester(`FALL-${y}`);
      else if (m < 8) setStartSemester(`FALL-${y}`);
      else setStartSemester(`SPRING-${y + 1}`);
    }

    const savedFEC = localStorage.getItem("freeElectiveCredits");
    if (savedFEC) setFreeElectiveCredits(Number(savedFEC));

    setLoading(true);
    Promise.all([
      fetch(apiUrl("/courses")).then((r) => r.json()),
      fetch(apiUrl(`/students/${id}/courses`)).then((r) => r.json()).catch(() => []),
    ]).then(([allCourses, savedIds]: [Course[], number[]]) => {
      setCourses(allCourses.filter((c: Course) => c.subject !== "ELEC"));
      if (Array.isArray(savedIds) && savedIds.length > 0) {
        setSelectedIds(new Set(savedIds));
      }
      setLoading(false);
    }).catch(() => setLoading(false));
  }, [router]);

  const subjects = useMemo(() => {
    const map = new Map<string, Course[]>();
    for (const c of courses) {
      const list = map.get(c.subject) || [];
      list.push(c);
      map.set(c.subject, list);
    }
    return Array.from(map.entries()).sort(([a], [b]) => a.localeCompare(b));
  }, [courses]);

  const filteredSubjects = useMemo(() => {
    if (!search) return subjects;
    const q = search.toLowerCase();
    return subjects
      .map(([subj, crses]) => [
        subj,
        crses.filter(
          (c) =>
            `${c.subject} ${c.number}`.toLowerCase().includes(q) ||
            c.title.toLowerCase().includes(q)
        ),
      ] as [string, Course[]])
      .filter(([, crses]) => crses.length > 0);
  }, [subjects, search]);

  const toggle = (id: number) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const selectedCredits = courses
    .filter((c) => selectedIds.has(c.courseId))
    .reduce((sum, c) => sum + c.credits, 0) + freeElectiveCredits;

  const semesterOptions = useMemo(() => {
    const options: { value: string; label: string }[] = [];
    const now = new Date();
    const baseYear = now.getFullYear();
    for (let y = baseYear; y <= baseYear + 3; y++) {
      options.push({ value: `SPRING-${y}`, label: `Spring ${y}` });
      options.push({ value: `SUMMER-${y}`, label: `Summer ${y}` });
      options.push({ value: `FALL-${y}`, label: `Fall ${y}` });
    }
    return options;
  }, []);

  const handleContinue = async () => {
    const studentId = localStorage.getItem("studentId");
    if (!studentId) return;

    setSaving(true);
    try {
      if (isCSMajor) {
        await fetch(apiUrl(`/students/${studentId}/capstone`), {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ capstoneChoice }),
        });
        localStorage.setItem("capstoneChoice", capstoneChoice);
      }

      localStorage.setItem("includeSummer", String(includeSummer));
      localStorage.setItem("startSemester", startSemester);
      localStorage.setItem("freeElectiveCredits", String(freeElectiveCredits));

      await fetch(apiUrl(`/students/${studentId}/free-elective-credits`), {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ freeElectiveCredits }),
      });

      if (selectedIds.size > 0) {
        const res = await fetch(apiUrl(`/students/${studentId}/courses`), {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(Array.from(selectedIds)),
        });
        if (!res.ok) throw new Error("Failed to save courses");
      }

      router.push("/dashboard");
    } catch {
      setSaving(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#0a0a0a] flex flex-col">
      {/* Header */}
      <header className="sticky top-0 z-20 bg-[#0a0a0a]/90 backdrop-blur-xl border-b border-[#1a1a1a]">
        <div className="max-w-5xl mx-auto px-6 py-4 flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold">Select Your Completed Courses</h1>
            <p className="text-sm text-zinc-500 mt-0.5">
              Choose all courses you&apos;ve already taken so we can build your roadmap accurately
            </p>
          </div>
          <div className="flex items-center gap-4">
            <div className="text-right">
              <div className="text-sm font-medium">
                {selectedIds.size} course{selectedIds.size !== 1 ? "s" : ""}
              </div>
              <div className="text-xs text-zinc-500">{selectedCredits} credits</div>
            </div>
            <button
              onClick={handleContinue}
              disabled={saving}
              className="bg-[#c8102e] text-white font-medium px-5 py-2.5 rounded-lg hover:bg-[#a00d24] disabled:opacity-50 transition-colors flex items-center gap-2"
            >
              {saving ? "Saving..." : "Generate Roadmap"}
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 8l4 4m0 0l-4 4m4-4H3" />
              </svg>
            </button>
          </div>
        </div>
      </header>

      <div className="max-w-5xl mx-auto w-full px-6 py-6 flex-1">
        {/* Capstone / Minor Selection (CS majors only) */}
        {isCSMajor && (
          <div className="mb-8 p-5 rounded-xl bg-[#111] border border-[#1e1e1e]">
            <h2 className="text-sm font-semibold text-zinc-200 mb-1">CS Senior Sequence / Minor</h2>
            <p className="text-xs text-zinc-500 mb-4">
              As a Computer Science major, choose a senior capstone sequence or a minor to complete your degree.
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              {CAPSTONE_OPTIONS.map((opt) => (
                <button
                  key={opt.value}
                  onClick={() => setCapstoneChoice(opt.value)}
                  className={`text-left p-4 rounded-lg border transition-all ${
                    capstoneChoice === opt.value
                      ? "bg-[#c8102e]/10 border-[#c8102e]/40"
                      : "bg-[#0a0a0a] border-[#222] hover:border-[#333]"
                  }`}
                >
                  <div className="flex items-center gap-2 mb-1.5">
                    <div className={`w-4 h-4 rounded-full border-2 flex items-center justify-center shrink-0 ${
                      capstoneChoice === opt.value ? "border-[#c8102e]" : "border-[#333]"
                    }`}>
                      {capstoneChoice === opt.value && <div className="w-2 h-2 bg-[#c8102e] rounded-full" />}
                    </div>
                    <span className="text-sm font-medium">{opt.label}</span>
                  </div>
                  <p className="text-[11px] text-zinc-500 ml-6">{opt.desc}</p>
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Scheduling Preferences */}
        <div className="mb-8 p-5 rounded-xl bg-[#111] border border-[#1e1e1e]">
          <h2 className="text-sm font-semibold text-zinc-200 mb-1">Scheduling Preferences</h2>
          <p className="text-xs text-zinc-500 mb-4">Set when your plan should begin and whether to use summer semesters.</p>
          <div className="flex flex-wrap items-center gap-6">
            <div className="flex items-center gap-3">
              <label className="text-xs text-zinc-400 whitespace-nowrap">Start Semester</label>
              <select
                value={startSemester}
                onChange={(e) => setStartSemester(e.target.value)}
                className="bg-[#0a0a0a] border border-[#222] rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-[#c8102e] transition-colors appearance-none"
              >
                {semesterOptions.map((opt) => (
                  <option key={opt.value} value={opt.value}>{opt.label}</option>
                ))}
              </select>
            </div>
            <div className="flex items-center gap-3">
              <button
                onClick={() => setIncludeSummer(!includeSummer)}
                className={`w-10 h-5 rounded-full transition-colors relative ${includeSummer ? "bg-[#c8102e]" : "bg-[#222]"}`}
              >
                <div className={`w-4 h-4 bg-white rounded-full absolute top-0.5 transition-transform ${includeSummer ? "translate-x-5" : "translate-x-0.5"}`} />
              </button>
              <span className="text-xs text-zinc-400">Include summer semesters</span>
            </div>
          </div>
        </div>

        {/* Free Elective Credits */}
        <div className="mb-8 p-5 rounded-xl bg-[#111] border border-[#1e1e1e]">
          <h2 className="text-sm font-semibold text-zinc-200 mb-1">Free Elective Credits</h2>
          <p className="text-xs text-zinc-500 mb-4">
            If you&apos;ve already earned free elective credits (AP, dual credit, transfer, etc.), enter the total here.
            This reduces the number of free elective slots on your roadmap.
          </p>
          <div className="flex items-center gap-3">
            <input
              type="number"
              min={0}
              max={30}
              value={freeElectiveCredits}
              onChange={(e) => setFreeElectiveCredits(Math.max(0, Math.min(30, Number(e.target.value) || 0)))}
              className="w-24 bg-[#0a0a0a] border border-[#222] rounded-lg px-3 py-2 text-sm text-center focus:outline-none focus:border-[#c8102e] transition-colors"
            />
            <span className="text-xs text-zinc-400">credit hours</span>
          </div>
        </div>

        {/* Search */}
        <div className="flex items-center gap-3 mb-6">
          <div className="flex-1 flex items-center bg-[#141414] border border-[#222] rounded-lg px-4 py-2.5">
            <svg className="w-4 h-4 text-zinc-500 mr-2.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by course code or title..."
              className="w-full bg-transparent text-sm focus:outline-none"
            />
          </div>
        </div>

        {/* Subject Filter Pills */}
        <div className="flex flex-wrap gap-2 mb-6">
          <button
            onClick={() => setActiveSubject(null)}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
              !activeSubject ? "bg-[#c8102e] text-white" : "bg-[#141414] border border-[#222] text-zinc-400 hover:text-white"
            }`}
          >
            All ({courses.length})
          </button>
          {subjects.map(([subj, crses]) => (
            <button
              key={subj}
              onClick={() => setActiveSubject(activeSubject === subj ? null : subj)}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                activeSubject === subj
                  ? "bg-[#c8102e] text-white"
                  : "bg-[#141414] border border-[#222] text-zinc-400 hover:text-white"
              }`}
            >
              {subj} ({crses.length})
            </button>
          ))}
        </div>

        {loading ? (
          <div className="text-center text-zinc-500 py-20">Loading courses...</div>
        ) : (
          <div className="space-y-8">
            {(activeSubject
              ? filteredSubjects.filter(([s]) => s === activeSubject)
              : filteredSubjects
            ).map(([subject, subjectCourses]) => (
              <div key={subject}>
                <div className="flex items-center gap-3 mb-3">
                  <h2 className="text-sm font-semibold text-zinc-300">{subject}</h2>
                  <div className="flex-1 h-px bg-[#1a1a1a]" />
                  <span className="text-xs text-zinc-600">
                    {subjectCourses.filter((c) => selectedIds.has(c.courseId)).length}/{subjectCourses.length} selected
                  </span>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                  {subjectCourses.map((course) => {
                    const isSelected = selectedIds.has(course.courseId);
                    return (
                      <button
                        key={course.courseId}
                        onClick={() => toggle(course.courseId)}
                        className={`text-left p-4 rounded-xl border transition-all ${
                          isSelected
                            ? "bg-[#c8102e]/10 border-[#c8102e]/40"
                            : "bg-[#141414] border-[#1e1e1e] hover:border-[#333]"
                        }`}
                      >
                        <div className="flex items-start justify-between gap-2">
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2">
                              <span className="text-sm font-semibold text-white">
                                {course.subject} {course.number}
                              </span>
                              <span className="text-[10px] text-zinc-600 bg-[#1a1a1a] px-1.5 py-0.5 rounded">
                                {course.credits} cr
                              </span>
                            </div>
                            <div className="text-xs text-zinc-500 mt-1 truncate">{course.title}</div>
                          </div>
                          <div
                            className={`w-5 h-5 rounded-md border-2 flex items-center justify-center shrink-0 transition-colors ${
                              isSelected
                                ? "bg-[#c8102e] border-[#c8102e]"
                                : "border-[#333] bg-transparent"
                            }`}
                          >
                            {isSelected && (
                              <svg className="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                              </svg>
                            )}
                          </div>
                        </div>
                      </button>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Sticky bottom bar with selected courses */}
      {selectedIds.size > 0 && (
        <div className="sticky bottom-0 bg-[#0f0f0f]/95 backdrop-blur-xl border-t border-[#1a1a1a] px-6 py-3">
          <div className="max-w-5xl mx-auto flex items-center gap-3 overflow-x-auto">
            <span className="text-xs text-zinc-500 shrink-0">Selected:</span>
            {courses
              .filter((c) => selectedIds.has(c.courseId))
              .map((c) => (
                <span
                  key={c.courseId}
                  className="inline-flex items-center gap-1.5 bg-[#1a1a1a] border border-[#222] text-xs px-2.5 py-1 rounded-full shrink-0"
                >
                  {c.subject} {c.number}
                  <button
                    onClick={() => toggle(c.courseId)}
                    className="text-zinc-500 hover:text-white transition-colors"
                  >
                    &times;
                  </button>
                </span>
              ))}
          </div>
        </div>
      )}
    </div>
  );
}
