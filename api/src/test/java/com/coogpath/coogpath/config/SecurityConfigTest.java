package com.coogpath.coogpath.config;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.coogpath.coogpath.controller.AuthController;
import com.coogpath.coogpath.controller.AdvisorController;
import com.coogpath.coogpath.controller.StudentController;
import com.coogpath.coogpath.dto.StudentProfile;
import com.coogpath.coogpath.model.Student;
import com.coogpath.coogpath.repository.StudentRepository;
import com.coogpath.coogpath.service.StudentService;
import com.coogpath.coogpath.service.AdvisorService;
import com.coogpath.coogpath.service.TokenService;

@WebMvcTest(
        controllers = { StudentController.class, AuthController.class, AdvisorController.class },
        properties = "app.rate-limit.auth-per-minute=3")
@Import({ SecurityConfig.class, JwtConfig.class, TokenService.class })
class SecurityConfigTest {

    @Autowired private MockMvc mvc;
    @Autowired private TokenService tokenService;

    @MockitoBean private StudentService studentService;
    @MockitoBean private AdvisorService advisorService;
    @MockitoBean private StudentRepository studentRepository;

    private static Student student(long id) {
        Student student = new Student();
        student.setStudentId(id);
        student.setName("Student " + id);
        student.setEmail("student" + id + "@uh.edu");
        return student;
    }

    private String bearer(long studentId) {
        return "Bearer " + tokenService.issue(student(studentId)).token();
    }

    @Test
    void student_endpoints_require_a_token() throws Exception {
        mvc.perform(get("/api/students/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(SecurityConfig.SIGN_IN_REQUIRED));
    }

    @Test
    void a_tampered_token_is_rejected() throws Exception {
        mvc.perform(get("/api/students/1").header("Authorization", bearer(1) + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void students_can_read_their_own_profile() throws Exception {
        when(studentService.getProfile(1L)).thenReturn(StudentProfile.from(student(1)));

        mvc.perform(get("/api/students/1").header("Authorization", bearer(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(1));
    }

    @Test
    void students_cannot_read_someone_elses_data() throws Exception {
        mvc.perform(get("/api/students/2").header("Authorization", bearer(1)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/students/2/transcript").header("Authorization", bearer(1)))
                .andExpect(status().isForbidden());
    }

    @Test
    void advisor_requires_own_student_token() throws Exception {
        String body = "{\"question\":\"What is next?\"}";
        mvc.perform(post("/api/advisor/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/advisor/2").header("Authorization", bearer(1))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_is_public_and_rate_limited() throws Exception {
        when(studentRepository.findByEmail("nobody@uh.edu")).thenReturn(Optional.empty());
        String body = "{\"email\":\"nobody@uh.edu\",\"password\":\"wrong-password\"}";

        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }
}
