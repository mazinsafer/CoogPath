export interface Course {
  courseId: number;
  subject: string;
  number: string;
  title: string;
  credits: number;
}

export interface DegreeProgram {
  programId: number;
  name: string;
  college: string;
  totalCreditsRequired: number;
}
