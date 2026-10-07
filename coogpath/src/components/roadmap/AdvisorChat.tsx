import { useState, type FormEvent } from "react";
import { askAdvisor, type AdvisorMessage } from "../../services/advisorService";
import type { PlanOptions } from "../../types/plan";
import { Button } from "../ui/Button";
import { Card, CardHeader } from "../ui/Card";

export function AdvisorChat({ studentId, options }: { studentId: number; options: PlanOptions }) {
  const [messages, setMessages] = useState<AdvisorMessage[]>([]);
  const [question, setQuestion] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function send(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const text = question.trim();
    if (!text || busy) return;
    setBusy(true);
    setError("");
    try {
      const { answer } = await askAdvisor(studentId, text, options, messages);
      setMessages((previous) => [...previous, { role: "user", content: text }, { role: "assistant", content: answer }]);
      setQuestion("");
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "The advisor is unavailable right now. Please try again.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <Card>
      <CardHeader title="Ask your roadmap advisor" description="Ask about your planned terms, courses, credits, or degree requirements." />
      <div className="space-y-4 p-5">
        {messages.length === 0 ? (
          <p className="text-sm text-zinc-500">Try “What courses are in my next term?” or “Which requirements have I completed?”</p>
        ) : (
          <div className="max-h-96 space-y-3 overflow-y-auto" role="log" aria-label="Advisor conversation" aria-live="polite">
            {messages.map((message, index) => (
              <div key={index} className={`rounded-lg px-4 py-3 text-sm whitespace-pre-wrap ${message.role === "user" ? "ml-8 bg-zinc-100 text-zinc-900" : "mr-8 border border-zinc-200 bg-white text-zinc-700"}`}>
                <span className="mb-1 block text-xs font-semibold text-zinc-500">{message.role === "user" ? "You" : "Advisor"}</span>
                {message.content}
              </div>
            ))}
          </div>
        )}
        <form onSubmit={send} className="flex flex-col gap-2 sm:flex-row sm:items-end">
          <label htmlFor="advisor-question" className="sr-only">Question for the roadmap advisor</label>
          <textarea
            id="advisor-question"
            value={question}
            onChange={(event) => setQuestion(event.target.value)}
            maxLength={1000}
            rows={2}
            placeholder="Ask a question about your roadmap…"
            className="min-h-16 flex-1 resize-y rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-600/20"
          />
          <Button type="submit" loading={busy} disabled={!question.trim()}>Ask advisor</Button>
        </form>
        {error && <p className="text-sm text-red-700" role="alert">{error}</p>}
        <p className="text-xs text-zinc-500">Answers use your current generated plan and degree requirements. Check academic decisions with an official advisor.</p>
      </div>
    </Card>
  );
}
