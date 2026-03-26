"use client";

import { useState, useEffect, useRef } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

interface DegreeProgram {
  programId: number;
  name: string;
  college: string;
  catalogYearStart: number;
  catalogYearEnd: number;
  totalCreditsRequired: number;
}

interface Course {
  courseId: number;
  subject: string;
  number: string;
  title: string;
  credits: number;
}

export default function SignupPage() {
  const router = useRouter();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [programId, setProgramId] = useState<number | null>(null);
  const [catalogYear, setCatalogYear] = useState(2024);
  const [includeSummer, setIncludeSummer] = useState(false);
  const [completedCourseIds, setCompletedCourseIds] = useState<number[]>([]);
  const [programs, setPrograms] = useState<DegreeProgram[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [courseSearch, setCourseSearch] = useState("");
  const [showCourseDropdown, setShowCourseDropdown] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetch("/api/programs")
      .then((r) => r.json())
      .then(setPrograms)
      .catch(() => {});
    fetch("/api/courses")
      .then((r) => r.json())
      .then(setCourses)
      .catch(() => {});
  }, []);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setShowCourseDropdown(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const filteredCourses = courses.filter(
    (c) =>
      !completedCourseIds.includes(c.courseId) &&
      (`${c.subject} ${c.number}`.toLowerCase().includes(courseSearch.toLowerCase()) ||
        c.title.toLowerCase().includes(courseSearch.toLowerCase()))
  );

  const selectedCourses = courses.filter((c) => completedCourseIds.includes(c.courseId));

  const removeCourse = (id: number) => {
    setCompletedCourseIds((prev) => prev.filter((cid) => cid !== id));
  };

  const handleSubmit = async () => {
    if (!name || !email || !password || !programId) {
      setError("Please fill in all required fields");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const res = await fetch("/api/students/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, email, password, programId, catalogYear, includeSummer }),
      });
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || "Registration failed");
      }
      const student = await res.json();
      localStorage.setItem("studentId", student.studentId);
      localStorage.setItem("studentName", student.name);
      router.push("/dashboard");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#0a0a0a] flex">
      {/* Left Panel */}
      <div className="hidden lg:flex flex-col justify-between w-1/2 p-12">
        <div>
          <Link href="/" className="flex items-center gap-2 mb-20">
            <div className="w-8 h-8 bg-[#c8102e] rounded-lg flex items-center justify-center text-xs font-bold">
              CP
            </div>
            <span className="text-lg font-semibold">CoogPath</span>
          </Link>

          <h1 className="text-4xl font-bold leading-tight mb-4">
            Let&apos;s Build Your
            <br />
            Graduation Roadmap
          </h1>
          <p className="text-zinc-400 text-lg mb-12 max-w-md">
            Tell us about your degree progress, and our AI will generate a personalized
            path to graduation in seconds.
          </p>

          <div className="space-y-6">
            {[
              { title: "Smart Course Sequencing", desc: "AI arranges courses based on prerequisites and availability" },
              { title: "Bottleneck Prevention", desc: "Identifies courses that could delay graduation" },
              { title: "Real-Time Adjustments", desc: "Adapts to course changes and your preferences" },
            ].map((item) => (
              <div key={item.title} className="flex items-start gap-3">
                <div className="w-2.5 h-2.5 bg-[#c8102e] rounded-full mt-1.5 shrink-0" />
                <div>
                  <div className="font-medium text-sm">{item.title}</div>
                  <div className="text-sm text-zinc-500">{item.desc}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
        <p className="text-sm text-zinc-700">&copy; 2026 University of Houston</p>
      </div>

      {/* Right Panel - Form */}
      <div className="flex-1 flex items-center justify-center p-8 lg:p-12">
        <div className="w-full max-w-md">
          <h2 className="text-2xl font-bold mb-1">Tell us about your degree</h2>
          <p className="text-zinc-500 text-sm mb-8">
            We&apos;ll use this information to create your personalized roadmap
          </p>

          {error && (
            <div className="bg-red-500/10 border border-red-500/20 text-red-400 rounded-lg px-4 py-3 text-sm mb-6">
              {error}
            </div>
          )}

          <div className="space-y-5">
            {/* Full Name */}
            <div>
              <label className="block text-sm font-medium text-zinc-300 mb-1.5">Full Name</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Jorge Ramirez"
                className="w-full bg-[#141414] border border-[#222] rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-[#c8102e] transition-colors"
              />
            </div>

            {/* UH Email */}
            <div>
              <label className="block text-sm font-medium text-zinc-300 mb-1.5">UH Email</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="jramirez@cougarnet.uh.edu"
                className="w-full bg-[#141414] border border-[#222] rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-[#c8102e] transition-colors"
              />
            </div>

            {/* Password */}
            <div>
              <label className="block text-sm font-medium text-zinc-300 mb-1.5">Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Create a password"
                className="w-full bg-[#141414] border border-[#222] rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-[#c8102e] transition-colors"
              />
            </div>

            {/* Select Major */}
            <div>
              <label className="block text-sm font-medium text-zinc-300 mb-1.5">Select Major</label>
              <select
                value={programId ?? ""}
                onChange={(e) => setProgramId(Number(e.target.value) || null)}
                className="w-full bg-[#141414] border border-[#222] rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-[#c8102e] transition-colors appearance-none"
              >
                <option value="">Choose your major</option>
                {programs.map((p) => (
                  <option key={p.programId} value={p.programId}>
                    {p.name}
                  </option>
                ))}
              </select>
            </div>

            {/* Catalog Year */}
            <div>
              <label className="block text-sm font-medium text-zinc-300 mb-1.5">Catalog Year</label>
              <div className="grid grid-cols-3 gap-2">
                {[2023, 2024, 2025].map((year) => (
                  <button
                    key={year}
                    onClick={() => setCatalogYear(year)}
                    className={`py-2.5 rounded-lg text-sm font-medium transition-colors ${
                      catalogYear === year
                        ? "bg-[#c8102e] text-white"
                        : "bg-[#141414] border border-[#222] text-zinc-400 hover:text-white hover:border-[#333]"
                    }`}
                  >
                    {year}
                  </button>
                ))}
              </div>
            </div>

            {/* Include Summer */}
            <div className="flex items-center gap-3">
              <button
                onClick={() => setIncludeSummer(!includeSummer)}
                className={`w-10 h-5 rounded-full transition-colors relative ${
                  includeSummer ? "bg-[#c8102e]" : "bg-[#222]"
                }`}
              >
                <div
                  className={`w-4 h-4 bg-white rounded-full absolute top-0.5 transition-transform ${
                    includeSummer ? "translate-x-5" : "translate-x-0.5"
                  }`}
                />
              </button>
              <span className="text-sm text-zinc-400">Include summer semesters</span>
            </div>

            {/* Add Completed Courses */}
            <div ref={dropdownRef}>
              <label className="block text-sm font-medium text-zinc-300 mb-1.5">Add Completed Courses</label>
              <div className="relative">
                <div className="flex items-center bg-[#141414] border border-[#222] rounded-lg px-4 py-2.5">
                  <svg className="w-4 h-4 text-zinc-500 mr-2 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                  </svg>
                  <input
                    type="text"
                    value={courseSearch}
                    onChange={(e) => {
                      setCourseSearch(e.target.value);
                      setShowCourseDropdown(true);
                    }}
                    onFocus={() => setShowCourseDropdown(true)}
                    placeholder="Search courses, e.g., COSC 1436"
                    className="w-full bg-transparent text-sm focus:outline-none"
                  />
                </div>

                {showCourseDropdown && filteredCourses.length > 0 && (
                  <div className="absolute z-50 top-full left-0 right-0 mt-1 bg-[#1a1a1a] border border-[#222] rounded-lg max-h-48 overflow-y-auto shadow-xl">
                    {filteredCourses.slice(0, 20).map((c) => (
                      <button
                        key={c.courseId}
                        onClick={() => {
                          setCompletedCourseIds((prev) => [...prev, c.courseId]);
                          setCourseSearch("");
                          setShowCourseDropdown(false);
                        }}
                        className="w-full text-left px-4 py-2.5 text-sm hover:bg-[#222] transition-colors flex items-center justify-between"
                      >
                        <span>
                          <span className="text-white font-medium">{c.subject} {c.number}</span>
                          <span className="text-zinc-500 ml-2">{c.title}</span>
                        </span>
                        <span className="text-xs text-zinc-600">{c.credits} cr</span>
                      </button>
                    ))}
                  </div>
                )}
              </div>

              {selectedCourses.length > 0 && (
                <div className="flex flex-wrap gap-2 mt-3">
                  {selectedCourses.map((c) => (
                    <span
                      key={c.courseId}
                      className="inline-flex items-center gap-1.5 bg-[#1a1a1a] border border-[#222] text-sm px-3 py-1 rounded-full"
                    >
                      {c.subject} {c.number}
                      <button
                        onClick={() => removeCourse(c.courseId)}
                        className="text-zinc-500 hover:text-white transition-colors"
                      >
                        &times;
                      </button>
                    </span>
                  ))}
                </div>
              )}
            </div>

            {/* Submit */}
            <button
              onClick={handleSubmit}
              disabled={loading}
              className="w-full bg-[#c8102e] text-white font-medium py-3 rounded-lg hover:bg-[#a00d24] disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2 mt-2"
            >
              {loading ? (
                "Creating..."
              ) : (
                <>
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                  </svg>
                  Initialize AI Engine
                </>
              )}
            </button>

            <p className="text-xs text-zinc-600 text-center">
              By continuing, you agree to UH&apos;s Terms of Service and Privacy Policy
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
