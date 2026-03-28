"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

import { apiUrl } from "@/lib/api";

interface DegreeProgram {
  programId: number;
  name: string;
  college: string;
}

function isUHEmail(email: string): boolean {
  const lower = email.toLowerCase().trim();
  return lower.endsWith("@uh.edu") || lower.endsWith("@cougarnet.uh.edu");
}

export default function SignupPage() {
  const router = useRouter();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [programId, setProgramId] = useState<number | null>(null);
  const [programs, setPrograms] = useState<DegreeProgram[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetch(apiUrl("/programs"))
      .then((r) => r.json())
      .then(setPrograms)
      .catch(() => {});
  }, []);

  const handleSubmit = async () => {
    if (!name || !email || !password || !programId) {
      setError("Please fill in all required fields");
      return;
    }
    if (!isUHEmail(email)) {
      setError("Please use a valid UH email address (@uh.edu or @cougarnet.uh.edu)");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(apiUrl("/students/register"), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, email, password, programId, catalogYear: 2024 }),
      });
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || "Registration failed");
      }
      const data = await res.json();
      localStorage.setItem("studentId", data.studentId);
      localStorage.setItem("studentName", data.name);
      if (data.programName) {
        localStorage.setItem("programName", data.programName);
      } else {
        const selectedProgram = programs.find((p) => p.programId === programId);
        if (selectedProgram) localStorage.setItem("programName", selectedProgram.name);
      }
      router.push("/courses");
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
          <a href="/" className="flex items-center gap-2 mb-20">
            <div className="w-8 h-8 bg-[#c8102e] rounded-lg flex items-center justify-center text-xs font-bold">CP</div>
            <span className="text-lg font-semibold">CoogPath</span>
          </a>
          <h1 className="text-4xl font-bold leading-tight mb-4">
            Let&apos;s Build Your<br />Graduation Roadmap
          </h1>
          <p className="text-zinc-400 text-lg mb-12 max-w-md">
            Tell us about your degree progress, and our AI will generate a personalized path to graduation in seconds.
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

      {/* Right Panel */}
      <div className="flex-1 flex items-center justify-center p-8 lg:p-12">
        <div className="w-full max-w-md">
          <h2 className="text-2xl font-bold mb-1">Create your account</h2>
          <p className="text-zinc-500 text-sm mb-8">Sign up to get your personalized graduation roadmap</p>

          {error && (
            <div className="bg-red-500/10 border border-red-500/20 text-red-400 rounded-lg px-4 py-3 text-sm mb-6">{error}</div>
          )}

          <div className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-zinc-300 mb-1.5">Full Name</label>
              <input type="text" value={name} onChange={(e) => setName(e.target.value)} placeholder="Jorge Ramirez"
                className="w-full bg-[#141414] border border-[#222] rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-[#c8102e] transition-colors" />
            </div>

            <div>
              <label className="block text-sm font-medium text-zinc-300 mb-1.5">UH Email</label>
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="jramirez@cougarnet.uh.edu"
                className="w-full bg-[#141414] border border-[#222] rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-[#c8102e] transition-colors" />
              <p className="text-[10px] text-zinc-600 mt-1">Must be a @uh.edu or @cougarnet.uh.edu address</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-zinc-300 mb-1.5">Password</label>
              <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Create a password"
                className="w-full bg-[#141414] border border-[#222] rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-[#c8102e] transition-colors" />
            </div>

            <div>
              <label className="block text-sm font-medium text-zinc-300 mb-1.5">Select Major</label>
              <select value={programId ?? ""} onChange={(e) => setProgramId(Number(e.target.value) || null)}
                className="w-full bg-[#141414] border border-[#222] rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-[#c8102e] transition-colors appearance-none">
                <option value="">Choose your major</option>
                {programs.map((p) => (
                  <option key={p.programId} value={p.programId}>{p.name}</option>
                ))}
              </select>
            </div>

            <button onClick={handleSubmit} disabled={loading}
              className="w-full bg-[#c8102e] text-white font-medium py-3 rounded-lg hover:bg-[#a00d24] disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2 mt-2">
              {loading ? "Creating account..." : "Continue"}
              {!loading && (
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 8l4 4m0 0l-4 4m4-4H3" />
                </svg>
              )}
            </button>

            <p className="text-xs text-zinc-600 text-center">
              Already have an account?{" "}
              <a href="/login" className="text-[#c8102e] hover:underline">Sign in</a>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
