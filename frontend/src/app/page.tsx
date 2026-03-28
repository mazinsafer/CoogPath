"use client";

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-[#0a0a0a]">
      {/* Navbar */}
      <nav className="flex items-center justify-between px-8 py-5 max-w-7xl mx-auto">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 bg-[#c8102e] rounded-lg flex items-center justify-center text-xs font-bold">
            CP
          </div>
          <span className="text-lg font-semibold tracking-tight">CoogPath</span>
          <span className="text-zinc-600 text-lg">.</span>
        </div>
        <div className="flex items-center gap-4">
          <a href="/login" className="text-sm text-zinc-400 hover:text-white transition-colors">
            Login
          </a>
          <a
            href="/signup"
            className="bg-[#c8102e] text-white text-sm font-medium px-5 py-2 rounded-lg hover:bg-[#a00d24] transition-colors"
          >
            Get Started
          </a>
        </div>
      </nav>

      {/* Hero */}
      <section className="max-w-7xl mx-auto px-8 pt-20 pb-16">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
          <div>
            <div className="inline-flex items-center gap-2 bg-[#141414] border border-[#222] rounded-full px-4 py-1.5 mb-8">
              <span className="w-2 h-2 bg-[#22c55e] rounded-full" />
              <span className="text-sm text-zinc-400">AI-Powered Degree Planning</span>
            </div>
            <h1 className="text-5xl lg:text-6xl font-bold leading-tight tracking-tight mb-6">
              Graduate on Time,
              <br />
              Every Time<span className="text-[#c8102e]">.</span>
            </h1>
            <p className="text-zinc-400 text-lg leading-relaxed mb-10 max-w-lg">
              CoogPath is the University of Houston&apos;s official AI-driven degree
              planner. Get a personalized roadmap that adapts to your goals, detects
              bottlenecks, and keeps you on track.
            </p>
            <div className="flex items-center gap-4">
              <a
                href="/signup"
                className="bg-[#c8102e] text-white font-medium px-6 py-3 rounded-lg hover:bg-[#a00d24] transition-colors flex items-center gap-2"
              >
                Generate My Roadmap
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 8l4 4m0 0l-4 4m4-4H3" />
                </svg>
              </a>
            </div>
          </div>

          {/* Roadmap Preview Card */}
          <div className="bg-[#141414] border border-[#222] rounded-2xl p-6 shadow-2xl">
            <div className="flex items-center justify-between mb-6">
              <span className="font-semibold">Your Roadmap</span>
              <span className="text-sm text-zinc-500">BS Computer Science</span>
            </div>
            {["Fall 2024", "Spring 2025", "Fall 2025", "Spring 2026"].map((term, i) => (
              <div key={term} className="mb-4">
                <div className="flex items-center justify-between mb-2">
                  <span className={`text-sm font-medium ${i === 0 ? "text-[#22c55e]" : "text-zinc-400"}`}>
                    {term}
                  </span>
                  <span className="text-xs text-zinc-600">15 credits</span>
                </div>
                <div className="grid grid-cols-4 gap-2">
                  {[...Array(i === 0 ? 5 : i === 1 ? 4 : 3)].map((_, j) => (
                    <div
                      key={j}
                      className={`h-7 rounded-md ${
                        i === 0 ? "bg-[#22c55e]/20 border border-[#22c55e]/30" : "bg-[#1e1e1e] border border-[#2a2a2a]"
                      }`}
                    />
                  ))}
                </div>
              </div>
            ))}
            <div className="flex items-center gap-2 mt-4 pt-4 border-t border-[#222]">
              <div className="w-4 h-4 bg-[#22c55e]/20 rounded-full flex items-center justify-center">
                <div className="w-2 h-2 bg-[#22c55e] rounded-full" />
              </div>
              <span className="text-sm text-zinc-400">
                AI detected optimal path: <span className="text-white font-medium">Spring 2027</span>
              </span>
            </div>
          </div>
        </div>
      </section>

      {/* Stats */}
      <section className="max-w-7xl mx-auto px-8 py-12">
        <div className="border-t border-[#222] pt-12">
          <div className="grid grid-cols-3 gap-8 max-w-2xl">
            {[
              { value: "12,000+", label: "Students Helped" },
              { value: "95%", label: "On-Time Graduation" },
              { value: "4.8/5", label: "Student Rating" },
            ].map((stat) => (
              <div key={stat.label}>
                <div className="text-3xl font-bold">{stat.value}</div>
                <div className="text-sm text-zinc-500 mt-1">{stat.label}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Feature Cards */}
      <section className="max-w-7xl mx-auto px-8 py-16">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {[
            {
              icon: (
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9.75 3.104v5.714a2.25 2.25 0 01-.659 1.591L5 14.5M9.75 3.104c-.251.023-.501.05-.75.082m.75-.082a24.301 24.301 0 014.5 0m0 0v5.714c0 .597.237 1.17.659 1.591L19.8 15.3M14.25 3.104c.251.023.501.05.75.082M19.8 15.3l-1.57.393A9.065 9.065 0 0112 15a9.065 9.065 0 00-6.23.693L5 14.5m14.8.8l1.402 1.402c1.232 1.232.65 3.318-1.067 3.611A48.309 48.309 0 0112 21c-2.773 0-5.491-.235-8.135-.687-1.718-.293-2.3-2.379-1.067-3.61L5 14.5" />
                </svg>
              ),
              title: "AI-Powered Planning",
              desc: "Our AI engine analyzes course prerequisites, availability, and your goals to create the optimal path to graduation.",
              color: "#c8102e",
            },
            {
              icon: (
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
                </svg>
              ),
              title: "Bottleneck Detection",
              desc: "Automatically identifies courses that could delay your graduation and suggests alternative schedules.",
              color: "#c8102e",
            },
            {
              icon: (
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75m-18 0v-7.5A2.25 2.25 0 015.25 9h13.5A2.25 2.25 0 0121 11.25v7.5" />
                </svg>
              ),
              title: "Real-Time Updates",
              desc: "Syncs with UH course catalog and automatically adjusts your plan when courses change or fill up.",
              color: "#c8102e",
            },
          ].map((feature) => (
            <div
              key={feature.title}
              className="bg-[#141414] border border-[#222] rounded-2xl p-8 hover:border-[#333] transition-colors"
            >
              <div
                className="w-12 h-12 rounded-xl flex items-center justify-center mb-5"
                style={{ background: `${feature.color}15`, color: feature.color }}
              >
                {feature.icon}
              </div>
              <h3 className="text-lg font-semibold mb-2">{feature.title}</h3>
              <p className="text-sm text-zinc-500 leading-relaxed">{feature.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Footer */}
      <footer className="max-w-7xl mx-auto px-8 py-8 border-t border-[#222]">
        <p className="text-sm text-zinc-600">&copy; 2026 University of Houston. All rights reserved.</p>
      </footer>
    </div>
  );
}
