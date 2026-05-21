package com.lqm.user_service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.lqm.user_service.BaseIntegrationTest;
import com.lqm.user_service.models.Role;
import com.lqm.user_service.models.Student;
import com.lqm.user_service.models.User;
import com.lqm.user_service.repositories.StudentRepository;
import com.lqm.user_service.repositories.UserRepository;
import com.lqm.user_service.services.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test cho Admin User CRUD endpoints:
 * - GET /api/internal/admin/users
 * - GET /api/internal/admin/users/{id}/response
 * - POST /api/internal/admin/users (multipart)
 * - DELETE /api/internal/admin/users/{id}
 * - GET /api/internal/admin/users/stats
 * - GET /api/internal/admin/users/me
 *
 * Dùng headers X-User-Email + X-User-Role để simulate
 * {@link com.lqm.user_service.filters.AuthFilter}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("User CRUD Flow — Integration Tests")
class UserCrudFlowIT extends BaseIntegrationTest {

        @MockitoBean
        CloudinaryService cloudinaryService;

        @MockitoBean
        GoogleIdTokenVerifier googleIdTokenVerifier;

        @Autowired
        MockMvc mockMvc;

        @Autowired
        ObjectMapper objectMapper;

        @Autowired
        UserRepository userRepository;

        @Autowired
        StudentRepository studentRepository;

        @Autowired
        BCryptPasswordEncoder passwordEncoder;

        private static final String ADMIN_EMAIL = "admin@ou.edu.vn";
        private static final String ADMIN_ROLE = "ROLE_ADMIN";
        private static final String TEACHER_EMAIL = "teacher.vana@ou.edu.vn";
        private static final String TEACHER_ROLE = "ROLE_TEACHER";

        private User adminUser;
        private User teacherUser;
        private User studentUser;

        @BeforeEach
        void setUp() {
                userRepository.deleteAll();
                userRepository.flush();

                when(cloudinaryService.uploadFile(any())).thenReturn(null);

                adminUser = User.builder()
                                .firstName("Admin").lastName("System")
                                .email(ADMIN_EMAIL)
                                .password(passwordEncoder.encode("admin123"))
                                .gender(true).role(Role.ROLE_ADMIN).active(true)
                                .build();

                teacherUser = User.builder()
                                .firstName("Nguyen").lastName("Van A")
                                .email(TEACHER_EMAIL)
                                .password(passwordEncoder.encode("teacher123"))
                                .gender(true).role(Role.ROLE_TEACHER).active(true)
                                .build();

                studentUser = User.builder()
                                .firstName("Le").lastName("Thi B")
                                .email("student.thib@ou.edu.vn")
                                .password(passwordEncoder.encode("student123"))
                                .gender(false).role(Role.ROLE_STUDENT).active(true)
                                .build();
                Student s = Student.builder().code("2054010001").user(studentUser).build();
                studentUser.setStudent(s);

                userRepository.saveAll(List.of(adminUser, teacherUser, studentUser));
        }

        // -----------------------------------------------------------------------
        // GET /api/internal/admin/users
        // -----------------------------------------------------------------------

        @Test
        @DisplayName("GET /api/internal/admin/users — admin → 200 + danh sách users")
        void getUsers_asAdmin_returns200WithUserList() throws Exception {
                mockMvc.perform(get("/api/internal/admin/users")
                                .header("X-User-Email", ADMIN_EMAIL)
                                .header("X-User-Role", ADMIN_ROLE))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.page.totalElements").value(3));
        }

        @Test
        @DisplayName("GET /api/internal/admin/users — không có auth header → 401")
        void getUsers_withoutAuth_returns401() throws Exception {
                mockMvc.perform(get("/api/internal/admin/users"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/internal/admin/users — teacher role → 403")
        void getUsers_asTeacher_returns403() throws Exception {
                mockMvc.perform(get("/api/internal/admin/users")
                                .header("X-User-Email", TEACHER_EMAIL)
                                .header("X-User-Role", TEACHER_ROLE))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/internal/admin/users?role=Teacher — filter theo role trả đúng")
        void getUsers_withRoleFilter_returnsFilteredUsers() throws Exception {
                mockMvc.perform(get("/api/internal/admin/users")
                                .param("role", "Teacher")
                                .header("X-User-Email", ADMIN_EMAIL)
                                .header("X-User-Role", ADMIN_ROLE))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.page.totalElements").value(1))
                                .andExpect(jsonPath("$.content[0].firstName").value("Nguyen"));
        }

        @Test
        @DisplayName("GET /api/internal/admin/users?kw=Van A — filter theo keyword trả đúng")
        void getUsers_withKeywordFilter_returnsFilteredUsers() throws Exception {
                mockMvc.perform(get("/api/internal/admin/users")
                                .param("kw", "Van A")
                                .header("X-User-Email", ADMIN_EMAIL)
                                .header("X-User-Role", ADMIN_ROLE))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.page.totalElements").value(1));
        }

        // -----------------------------------------------------------------------
        // GET /api/internal/admin/users/{id}/response
        // -----------------------------------------------------------------------

        @Test
        @DisplayName("GET /api/internal/admin/users/{id}/response — ID tồn tại → 200")
        void getUserById_existingId_returns200() throws Exception {
                mockMvc.perform(get("/api/internal/admin/users/{id}/response", teacherUser.getId())
                                .header("X-User-Email", ADMIN_EMAIL)
                                .header("X-User-Role", ADMIN_ROLE))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(teacherUser.getId().toString()))
                                .andExpect(jsonPath("$.firstName").value("Nguyen"));
        }

        @Test
        @DisplayName("GET /api/internal/admin/users/{id}/response — ID không tồn tại → 404")
        void getUserById_nonExistentId_returns404() throws Exception {
                mockMvc.perform(get("/api/internal/admin/users/{id}/response", UUID.randomUUID())
                                .header("X-User-Email", ADMIN_EMAIL)
                                .header("X-User-Role", ADMIN_ROLE))
                                .andExpect(status().isNotFound());
        }

        // -----------------------------------------------------------------------
        // DELETE /api/internal/admin/users/{id}
        // -----------------------------------------------------------------------

        @Test
        @DisplayName("DELETE /api/internal/admin/users/{id} — ID tồn tại → 200 + xóa khỏi DB")
        void deleteUser_existingId_returns200AndRemovesFromDb() throws Exception {
                UUID id = teacherUser.getId();

                mockMvc.perform(delete("/api/internal/admin/users/{id}", id)
                                .header("X-User-Email", ADMIN_EMAIL)
                                .header("X-User-Role", ADMIN_ROLE))
                                .andExpect(status().isOk());

                assertThat(userRepository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("DELETE /api/internal/admin/users/{id} — xóa student thì Student record cũng bị xóa")
        void deleteUser_studentUser_cascadeDeletesStudentRecord() throws Exception {
                UUID studentId = studentUser.getId();

                mockMvc.perform(delete("/api/internal/admin/users/{id}", studentId)
                                .header("X-User-Email", ADMIN_EMAIL)
                                .header("X-User-Role", ADMIN_ROLE))
                                .andExpect(status().isOk());

                assertThat(userRepository.findById(studentId)).isEmpty();
                assertThat(studentRepository.findById(studentId)).isEmpty();
        }

        @Test
        @DisplayName("DELETE /api/internal/admin/users/{id} — ID không tồn tại → 404")
        void deleteUser_nonExistentId_returns404() throws Exception {
                mockMvc.perform(delete("/api/internal/admin/users/{id}", UUID.randomUUID())
                                .header("X-User-Email", ADMIN_EMAIL)
                                .header("X-User-Role", ADMIN_ROLE))
                                .andExpect(status().isNotFound());
        }

        // -----------------------------------------------------------------------
        // GET /api/internal/admin/users/stats
        // -----------------------------------------------------------------------

        @Test
        @DisplayName("GET /api/internal/admin/users/stats — admin → 200 + statistics map")
        void getStats_asAdmin_returns200WithStats() throws Exception {
                mockMvc.perform(get("/api/internal/admin/users/stats")
                                .header("X-User-Email", ADMIN_EMAIL)
                                .header("X-User-Role", ADMIN_ROLE))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalUsers").value(3))
                                .andExpect(jsonPath("$.byRole").exists())
                                .andExpect(jsonPath("$.byStatus").exists());
        }

        // -----------------------------------------------------------------------
        // GET /api/internal/admin/users/me
        // -----------------------------------------------------------------------

        @Test
        @DisplayName("GET /api/internal/admin/users/me — admin → 200 + thông tin admin")
        void getMe_asAdmin_returns200WithCurrentUser() throws Exception {
                mockMvc.perform(get("/api/internal/admin/users/me")
                                .header("X-User-Email", ADMIN_EMAIL)
                                .header("X-User-Role", ADMIN_ROLE))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value(ADMIN_EMAIL));
        }
}
