export interface RequirementCourse {
  /** "SUBJ NUM", or "A or B" when any one course from a set satisfies the requirement. */
  courseCode: string;
  title: string;
  credits: number;
  completed: boolean;
}

export interface RequirementGroup {
  name: string;
  totalCredits: number;
  completedCredits: number;
  courses: RequirementCourse[];
}
