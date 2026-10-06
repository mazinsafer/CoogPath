import { formatTermLabel } from "../lib/terms";
import type { PlanResult } from "../types/plan";

const BRAND_RGB: [number, number, number] = [200, 16, 46];
const PAGE_BOTTOM = 275;

/** Builds a one-column PDF of the roadmap. jsPDF is loaded on demand to keep it out of the main bundle. */
export async function exportRoadmapPdf(plan: PlanResult, studentName: string, programName: string | null) {
  const { jsPDF } = await import("jspdf");
  const doc = new jsPDF();
  const pageWidth = doc.internal.pageSize.getWidth();
  const left = 16;
  const right = pageWidth - 16;
  let y = 22;

  const ensureSpace = (needed: number) => {
    if (y + needed > PAGE_BOTTOM) {
      doc.addPage();
      y = 20;
    }
  };

  doc.setFont("helvetica", "bold");
  doc.setFontSize(18);
  doc.setTextColor(...BRAND_RGB);
  doc.text("CoogPath degree roadmap", left, y);
  y += 8;

  doc.setFont("helvetica", "normal");
  doc.setFontSize(10);
  doc.setTextColor(90);
  doc.text([studentName, programName].filter(Boolean).join("  ·  "), left, y);
  doc.text(`Generated ${new Date().toLocaleDateString()}`, right, y, { align: "right" });
  y += 5;
  doc.setDrawColor(220);
  doc.line(left, y, right, y);
  y += 9;

  for (const term of plan.terms) {
    ensureSpace(14);
    doc.setFont("helvetica", "bold");
    doc.setFontSize(12);
    doc.setTextColor(25);
    doc.text(formatTermLabel(term.termLabel), left, y);
    doc.setFont("helvetica", "normal");
    doc.setFontSize(9);
    doc.setTextColor(120);
    doc.text(`${term.totalCredits} credits`, right, y, { align: "right" });
    y += 6;

    doc.setFontSize(10);
    for (const course of term.courses) {
      ensureSpace(6);
      doc.setTextColor(40);
      doc.text(course.courseCode, left + 2, y);
      doc.setTextColor(90);
      doc.text(doc.splitTextToSize(course.title, right - left - 60)[0] ?? "", left + 38, y);
      doc.text(`${course.credits} cr`, right, y, { align: "right" });
      y += 5.5;
    }
    y += 5;
  }

  if (plan.blockers.length > 0) {
    ensureSpace(14);
    doc.setFont("helvetica", "bold");
    doc.setFontSize(11);
    doc.setTextColor(...BRAND_RGB);
    doc.text("Could not be scheduled", left, y);
    y += 6;
    doc.setFont("helvetica", "normal");
    doc.setFontSize(10);
    doc.setTextColor(70);
    for (const blocker of plan.blockers) {
      ensureSpace(6);
      doc.text(`• ${blocker}`, left + 2, y);
      y += 5.5;
    }
  }

  doc.save(`coogpath-roadmap-${studentName.trim().replace(/\s+/g, "-").toLowerCase()}.pdf`);
}
