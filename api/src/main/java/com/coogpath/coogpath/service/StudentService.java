package com.coogpath.coogpath.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coogpath.coogpath.dto.StudentPreferencesRequest;
import com.coogpath.coogpath.dto.StudentProfile;
import com.coogpath.coogpath.dto.StudentRegistrationDTO;
import com.coogpath.coogpath.dto.TranscriptEntry;
import com.coogpath.coogpath.exception.ResourceNotFoundException;
import com.coogpath.coogpath.model.Course;
import com.coogpath.coogpath.model.DegreeProgram;
import com.coogpath.coogpath.model.Student;
import com.coogpath.coogpath.model.StudentCourse;
import com.coogpath.coogpath.repository.CourseRepository;
import com.coogpath.coogpath.repository.DegreeProgramRepository;
import com.coogpath.coogpath.repository.StudentCourseRepository;
import com.coogpath.coogpath.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentService
{
    static final int MIN_PASSWORD_LENGTH = 8;
    static final int MAX_FREE_ELECTIVE_CREDITS = 60;

    private final StudentRepository studentRepository;
    private final DegreeProgramRepository degreeProgramRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseRepository courseRepository;
    private final StudentCourseRepository studentCourseRepository;

    public Student addStudent(StudentRegistrationDTO dto)
    {
        String name = trimToNull(dto.getName());
        String email = trimToNull(dto.getEmail());
        if (name == null || email == null || dto.getPassword() == null || dto.getProgramId() == null) {
            throw new IllegalArgumentException("Name, email, password, and major are required.");
        }
        if (!isUhEmail(email)) {
            throw new IllegalArgumentException("Use a UH email address (@uh.edu or @cougarnet.uh.edu).");
        }
        if (dto.getPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        if (studentRepository.findByEmail(email).isPresent())
        {
            throw new IllegalArgumentException("Email is already in use.");
        }

        DegreeProgram program = degreeProgramRepository.findById(dto.getProgramId())
                .orElseThrow(() -> new IllegalArgumentException("Degree program not found"));

        Student student = new Student();
        student.setName(name);
        student.setEmail(email);
        student.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        student.setDegreeProgram(program);
        student.setCatalogYear(dto.getCatalogYear());
        student.setIncludeSummer(dto.isIncludeSummer());
        student.setCapstoneChoice(dto.getCapstoneChoice() != null ? dto.getCapstoneChoice() : ProgramRules.DEFAULT_CAPSTONE);

        return studentRepository.save(student);
    }

    public StudentProfile getProfile(Long studentId)
    {
        return StudentProfile.from(findStudent(studentId));
    }

    public StudentProfile updatePreferences(Long studentId, StudentPreferencesRequest request)
    {
        Student student = findStudent(studentId);

        if (request.capstoneChoice() != null) {
            if (!ProgramRules.CAPSTONE_CHOICES.contains(request.capstoneChoice())) {
                throw new IllegalArgumentException("Invalid capstone choice");
            }
            student.setCapstoneChoice(request.capstoneChoice());
        }
        if (request.financeTrack() != null) {
            if (!ProgramRules.isValidFinanceTrack(request.financeTrack())) {
                throw new IllegalArgumentException("Invalid finance track");
            }
            student.setFinanceTrack(request.financeTrack());
        }
        if (request.mathMinor() != null) {
            student.setMathMinor(request.mathMinor());
        }
        if (request.freeElectiveCredits() != null) {
            int credits = request.freeElectiveCredits();
            if (credits < 0 || credits > MAX_FREE_ELECTIVE_CREDITS) {
                throw new IllegalArgumentException(
                        "Free elective credits must be between 0 and " + MAX_FREE_ELECTIVE_CREDITS + ".");
            }
            student.setFreeElectiveCredits(credits);
        }
        if (request.includeSummer() != null) {
            student.setIncludeSummer(request.includeSummer());
        }

        return StudentProfile.from(studentRepository.save(student));
    }

    public List<TranscriptEntry> getTranscript(Long studentId)
    {
        findStudent(studentId);
        return studentCourseRepository.findByStudentStudentId(studentId).stream()
                .map(TranscriptEntry::from)
                .toList();
    }

    public List<Long> getCompletedCourseIds(Long studentId)
    {
        findStudent(studentId);
        return studentCourseRepository.findByStudentStudentId(studentId).stream()
                .map(sc -> sc.getCourse().getCourseId())
                .toList();
    }

    /** Replaces the student's completed-course list with exactly the given course IDs. */
    @Transactional
    public int replaceCompletedCourses(Long studentId, Collection<Long> courseIds)
    {
        Student student = findStudent(studentId);
        Set<Long> selected = new LinkedHashSet<>(courseIds);

        Map<Long, Course> coursesById = courseRepository.findAllById(selected).stream()
                .collect(Collectors.toMap(Course::getCourseId, Function.identity()));
        for (Long courseId : selected) {
            if (!coursesById.containsKey(courseId)) {
                throw new IllegalArgumentException("Course not found: " + courseId);
            }
        }

        studentCourseRepository.deleteAll(studentCourseRepository.findByStudentStudentId(studentId));
        studentCourseRepository.flush();

        LocalDateTime now = LocalDateTime.now();
        for (Long courseId : selected) {
            StudentCourse record = new StudentCourse();
            record.setStudent(student);
            record.setCourse(coursesById.get(courseId));
            record.setStatus(StudentCourse.Status.TAKEN);
            record.setAttemptNo(1);
            record.setCreatedAt(now);
            studentCourseRepository.save(record);
        }
        return selected.size();
    }

    private Student findStudent(Long studentId)
    {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    static boolean isUhEmail(String email)
    {
        String lower = email.toLowerCase();
        return lower.endsWith("@uh.edu") || lower.endsWith("@cougarnet.uh.edu");
    }

    private static String trimToNull(String value)
    {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
