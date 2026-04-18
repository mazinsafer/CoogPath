package com.coogpath.coogpath.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.transaction.annotation.Transactional;

import com.coogpath.coogpath.dto.StudentRegistrationDTO;
import com.coogpath.coogpath.model.Course;
import com.coogpath.coogpath.model.Student;
import com.coogpath.coogpath.model.StudentCourse;
import com.coogpath.coogpath.repository.CourseRepository;
import com.coogpath.coogpath.repository.StudentCourseRepository;
import com.coogpath.coogpath.repository.StudentRepository;
import com.coogpath.coogpath.service.StudentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final StudentCourseRepository studentCourseRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(@RequestBody StudentRegistrationDTO dto) {
        try {
            Student newStudent = studentService.addStudent(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("studentId", newStudent.getStudentId());
            response.put("name", newStudent.getName());
            response.put("programName", newStudent.getDegreeProgram().getName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred during registration.");
        }
    }

    @GetMapping("/{studentId}/transcript")
    public List<Map<String, Object>> getTranscript(@PathVariable Long studentId) {
        List<StudentCourse> records = studentCourseRepository.findByStudentStudentId(studentId);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (StudentCourse sc : records) {
            Map<String, Object> m = new HashMap<>();
            m.put("courseCode", sc.getCourse().getSubject() + " " + sc.getCourse().getNumber());
            m.put("title", sc.getCourse().getTitle());
            m.put("credits", sc.getCourse().getCredits());
            m.put("status", sc.getStatus().name());
            m.put("grade", sc.getGrade());
            result.add(m);
        }
        return result;
    }

    @PatchMapping("/{studentId}/capstone")
    public ResponseEntity<?> updateCapstoneChoice(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> body) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        String choice = body.get("capstoneChoice");
        if (choice == null || (!choice.equals("SENIOR_SE") && !choice.equals("SENIOR_DS") && !choice.equals("MATH_MINOR"))) {
            return ResponseEntity.badRequest().body("Invalid capstone choice");
        }
        student.setCapstoneChoice(choice);
        studentRepository.save(student);
        return ResponseEntity.ok(Map.of("capstoneChoice", choice));
    }

    @PatchMapping("/{studentId}/finance-track")
    public ResponseEntity<?> updateFinanceTrack(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> body) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        String track = body.get("financeTrack");
        if (track == null
                || (!track.equals("STANDARD")
                && !track.equals("RE")
                && !track.equals("PFP")
                && !track.equals("CBC")
                && !track.equals("GEM")
                && !track.equals("ECTC"))) {
            return ResponseEntity.badRequest().body("Invalid finance track");
        }
        student.setFinanceTrack(track);
        studentRepository.save(student);
        return ResponseEntity.ok(Map.of("financeTrack", track));
    }

    @PatchMapping("/{studentId}/math-minor")
    public ResponseEntity<?> updateMathMinor(
            @PathVariable Long studentId,
            @RequestBody Map<String, Boolean> body) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        Boolean wantMinor = body.get("mathMinor");
        student.setMathMinor(Boolean.TRUE.equals(wantMinor));
        studentRepository.save(student);
        return ResponseEntity.ok(Map.of("mathMinor", student.isMathMinor()));
    }

    @PatchMapping("/{studentId}/free-elective-credits")
    public ResponseEntity<?> updateFreeElectiveCredits(
            @PathVariable Long studentId,
            @RequestBody Map<String, Integer> body) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        Integer credits = body.get("freeElectiveCredits");
        student.setFreeElectiveCredits(credits != null ? credits : 0);
        studentRepository.save(student);
        return ResponseEntity.ok(Map.of("freeElectiveCredits", student.getFreeElectiveCredits()));
    }

    @GetMapping("/{studentId}/courses")
    public ResponseEntity<?> getCompletedCourses(@PathVariable Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        List<StudentCourse> records = studentCourseRepository.findByStudentStudentId(studentId);
        List<Long> courseIds = records.stream()
                .map(sc -> sc.getCourse().getCourseId())
                .toList();
        return ResponseEntity.ok(courseIds);
    }

    @Transactional
    @PostMapping("/{studentId}/courses")
    public ResponseEntity<?> saveCompletedCourses(
            @PathVariable Long studentId,
            @RequestBody List<Long> courseIds) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        java.util.Set<Long> selectedSet = new java.util.HashSet<>();
        for (Object raw : courseIds) {
            selectedSet.add(((Number) raw).longValue());
        }

        List<StudentCourse> existing = studentCourseRepository.findByStudentStudentId(studentId);

        // Remove ALL existing records, then re-add only the selected ones
        studentCourseRepository.deleteAll(existing);
        studentCourseRepository.flush();

        for (Long courseId : selectedSet) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

            StudentCourse sc = new StudentCourse();
            sc.setStudent(student);
            sc.setCourse(course);
            sc.setStatus(StudentCourse.Status.TAKEN);
            sc.setGrade("A");
            sc.setAttemptNo(1);
            sc.setCreatedAt(LocalDateTime.now());
            studentCourseRepository.save(sc);
        }

        return ResponseEntity.ok(Map.of("saved", selectedSet.size()));
    }
}
