package com.coogpath.coogpath.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.coogpath.coogpath.model.Course;
import com.coogpath.coogpath.model.RequirementGroup;
import com.coogpath.coogpath.model.Student;

/**
 * Program-specific rules that are not expressed in the requirement tables:
 * which optional requirement groups apply to a student (CS capstone choice,
 * Finance track, Finance math minor) and which subject a program treats as
 * its "core" subject when spreading courses across terms.
 *
 * Group selection is driven by requirement_group.name, so renaming a group
 * in a migration must be mirrored here.
 */
public final class ProgramRules {

    public static final long COMPUTER_SCIENCE_PROGRAM_ID = 1L;
    public static final long FINANCE_PROGRAM_ID = 2L;

    public static final String DEFAULT_CAPSTONE = "SENIOR_SE";
    public static final String DEFAULT_FINANCE_TRACK = "STANDARD";

    public static final Set<String> CAPSTONE_CHOICES = Set.of("SENIOR_SE", "SENIOR_DS", "MATH_MINOR");

    private static final Map<String, String> FINANCE_TRACK_NAMES = new LinkedHashMap<>();
    static {
        FINANCE_TRACK_NAMES.put("STANDARD", "Standard");
        FINANCE_TRACK_NAMES.put("RE", "Real Estate");
        FINANCE_TRACK_NAMES.put("PFP", "Personal Financial Planning");
        FINANCE_TRACK_NAMES.put("CBC", "Corporate Banking and Credit");
        FINANCE_TRACK_NAMES.put("GEM", "Global Energy Management");
        FINANCE_TRACK_NAMES.put("ECTC", "Energy Commodities Trading and Consulting");
    }

    private static final String FINANCE_TRACK_PREFIX = "Finance Track:";
    private static final String FINANCE_ELECTIVES_PREFIX = "Finance Bauer Electives:";
    private static final String FINANCE_MATH_MINOR_GROUP = "Finance Math Minor";
    private static final String CS_MATH_MINOR_GROUP = "Math Minor";
    private static final String ELECTIVE_PLACEHOLDER_SUBJECT = "ELEC";

    private ProgramRules() {
    }

    public static boolean isValidFinanceTrack(String track) {
        return track != null && FINANCE_TRACK_NAMES.containsKey(track);
    }

    public static String financeTrackDisplayName(String track) {
        return FINANCE_TRACK_NAMES.getOrDefault(track, FINANCE_TRACK_NAMES.get(DEFAULT_FINANCE_TRACK));
    }

    public static boolean isComputerScience(Student student) {
        return programIdOf(student) == COMPUTER_SCIENCE_PROGRAM_ID;
    }

    public static boolean isFinance(Student student) {
        return programIdOf(student) == FINANCE_PROGRAM_ID;
    }

    /** Subject the planner guarantees at least one course of in every fall/spring term. */
    public static String coreSubject(Student student) {
        return isFinance(student) ? "FINA" : "COSC";
    }

    /** Whether a requirement group counts toward this student's degree given their options. */
    public static boolean appliesTo(Student student, RequirementGroup group) {
        String name = group.getName();

        if (isComputerScience(student)) {
            String choice = student.getCapstoneChoice() != null ? student.getCapstoneChoice() : DEFAULT_CAPSTONE;
            boolean isSoftwareEngineering = name.contains("Software Engineering");
            boolean isDataScience = name.contains("Data Science");
            boolean isMathMinor = name.equals(CS_MATH_MINOR_GROUP);
            switch (choice) {
                case "SENIOR_DS":
                    return !isSoftwareEngineering && !isMathMinor;
                case "MATH_MINOR":
                    return !isSoftwareEngineering && !isDataScience;
                case "SENIOR_SE":
                default:
                    return !isDataScience && !isMathMinor;
            }
        }

        if (isFinance(student)) {
            String wantedTrack = financeTrackDisplayName(student.getFinanceTrack());
            if (name.startsWith(FINANCE_TRACK_PREFIX)) {
                return name.substring(FINANCE_TRACK_PREFIX.length()).trim().equalsIgnoreCase(wantedTrack);
            }
            if (name.startsWith(FINANCE_ELECTIVES_PREFIX)) {
                return name.substring(FINANCE_ELECTIVES_PREFIX.length()).trim().equalsIgnoreCase(wantedTrack);
            }
            if (name.equals(FINANCE_MATH_MINOR_GROUP)) {
                return student.isMathMinor();
            }
        }

        return true;
    }

    /**
     * Whether a requirement item's course still has to be taken separately.
     * For Finance students with the math minor, the minor's math courses fill
     * the ELEC-subject placeholders (ADV-ELEC, GEN-ELEC) in the Bauer Electives
     * group, so those placeholders are dropped. BUSI placeholders remain because
     * they must be business courses.
     */
    public static boolean countsSeparately(Student student, RequirementGroup group, Course course) {
        boolean minorCoversElectives = isFinance(student)
                && student.isMathMinor()
                && group.getName().startsWith(FINANCE_ELECTIVES_PREFIX);
        return !(minorCoversElectives && ELECTIVE_PLACEHOLDER_SUBJECT.equals(course.getSubject()));
    }

    private static long programIdOf(Student student) {
        if (student.getDegreeProgram() == null || student.getDegreeProgram().getProgramId() == null) {
            return -1L;
        }
        return student.getDegreeProgram().getProgramId();
    }
}
