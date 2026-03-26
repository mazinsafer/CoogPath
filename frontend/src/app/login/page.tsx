"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleLogin = async () => {
    if (!email || !password) {
      setError("Please fill in all fields");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      if (!res.ok) {
        throw new Error("Invalid email or password");
      }
      const data = await res.json();
      localStorage.setItem("studentId", data.studentId);
      localStorage.setItem("studentName", data.name);
      router.push("/dashboard");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#0a0a0a] flex items-center justify-center p-8">
      <div className="w-full max-w-sm">
        <Link href="/" className="flex items-center gap-2 mb-12 justify-center">
          <div className="w-8 h-8 bg-[#c8102e] rounded-lg flex items-center justify-center text-xs font-bold">
            CP
          </div>
          <span className="text-lg font-semibold">CoogPath</span>
        </Link>

        <h1 className="text-2xl font-bold text-center mb-1">Welcome back</h1>
        <p className="text-zinc-500 text-sm text-center mb-8">
          Sign in to view your degree roadmap
        </p>

        {error && (
          <div className="bg-red-500/10 border border-red-500/20 text-red-400 rounded-lg px-4 py-3 text-sm mb-6">
            {error}
          </div>
        )}

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-zinc-300 mb-1.5">UH Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && handleLogin()}
              placeholder="jramirez@cougarnet.uh.edu"
              className="w-full bg-[#141414] border border-[#222] rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-[#c8102e] transition-colors"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-zinc-300 mb-1.5">Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && handleLogin()}
              placeholder="Enter your password"
              className="w-full bg-[#141414] border border-[#222] rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-[#c8102e] transition-colors"
            />
          </div>

          <button
            onClick={handleLogin}
            disabled={loading}
            className="w-full bg-[#c8102e] text-white font-medium py-3 rounded-lg hover:bg-[#a00d24] disabled:opacity-50 disabled:cursor-not-allowed transition-colors mt-2"
          >
            {loading ? "Signing in..." : "Sign In"}
          </button>
        </div>

        <p className="text-sm text-zinc-600 text-center mt-6">
          Don&apos;t have an account?{" "}
          <Link href="/signup" className="text-[#c8102e] hover:underline">
            Get started
          </Link>
        </p>
      </div>
    </div>
  );
}
