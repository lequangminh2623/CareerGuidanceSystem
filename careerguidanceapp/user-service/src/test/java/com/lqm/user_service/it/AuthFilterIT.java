package com.lqm.user_service.it;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.lqm.user_service.BaseIntegrationTest;
import com.lqm.user_service.models.Role;
import com.lqm.user_service.models.User;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test cho {@link com.lqm.user_service.filters.AuthFilter}.
 *
 * AuthFilter đọc headers {@code X-User-Email} + {@code X-User-Role} để populate
 * SecurityContext. Test này xác minh toàn bộ security filter chain với DB thực:
 * - Endpoint public (/api/auth/**): không cần header
 * - Endpoint authenticated (/api/secure/**): cần cả hai header hợp lệ
 * - Endpoint admin (/api/internal/admin/**): cần role ROLE_ADMIN
 * - Thiếu header → 401; Sai role → 403
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("AuthFilter Security — Integration Tests")
class AuthFilterIT extends BaseIntegrationTest {

    @MockitoBean
    CloudinaryService cloudinaryService;

    @MockitoBean
    GoogleIdTokenVerifier googleIdTokenVerifier;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    private User adminUser;
    private User teacherUser;
    private User studentUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.flush();

        adminUser = User.builder()
                .firstName("Admin").lastName("System")
                .email("admin@ou.edu.vn")
                .password(passwordEncoder.encode("pass"))
                .gender(true).role(Role.ROLE_ADMIN).active(true)
                .build();

        teacherUser = User.builder()
                .firstName("Nguyen").lastName("Van A")
                .email("teacher.vana@ou.edu.vn")
                .password(passwordEncoder.encode("pass"))
                .gender(true).role(Role.ROLE_TEACHER).active(true)
                .build();

        studentUser = User.builder()
                .firstName("Le").lastName("Thi B")
                .email("student.thib@ou.edu.vn")
                .password(passwordEncoder.encode("pass"))
                .gender(false).role(Role.ROLE_STUDENT).active(true)
                .build();

        userRepository.saveAll(List.of(adminUser, teacherUser, studentUser));
    }

    // -----------------------------------------------------------------------
    // Authenticated endpoint — /api/secure/**
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("/api/secure/me — không có header → 401")
    void secureEndpoint_withoutAuthHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/secure/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/api/secure/me — ROLE_TEACHER header → 200")
    void secureEndpoint_withTeacherHeader_returns200() throws Exception {
        mockMvc.perform(get("/api/secure/me")
                        .header("X-User-Email", "teacher.vana@ou.edu.vn")
                        .header("X-User-Role", "ROLE_TEACHER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/api/secure/me — ROLE_STUDENT header → 200")
    void secureEndpoint_withStudentHeader_returns200() throws Exception {
        mockMvc.perform(get("/api/secure/me")
                        .header("X-User-Email", "student.thib@ou.edu.vn")
                        .header("X-User-Role", "ROLE_STUDENT"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/api/secure/me — chỉ có X-User-Email, thiếu X-User-Role → 401")
    void secureEndpoint_withEmailOnlyNoRole_returns401() throws Exception {
        mockMvc.perform(get("/api/secure/me")
                        .header("X-User-Email", "teacher.vana@ou.edu.vn"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/api/secure/me — chỉ có X-User-Role, thiếu X-User-Email → 401")
    void secureEndpoint_withRoleOnlyNoEmail_returns401() throws Exception {
        mockMvc.perform(get("/api/secure/me")
                        .header("X-User-Role", "ROLE_TEACHER"))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Admin-only endpoint — /api/internal/admin/**
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("/api/internal/admin/users — ROLE_ADMIN → 200")
    void adminEndpoint_withAdminRole_returns200() throws Exception {
        mockMvc.perform(get("/api/internal/admin/users")
                        .header("X-User-Email", "admin@ou.edu.vn")
                        .header("X-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/api/internal/admin/users — ROLE_TEACHER → 403")
    void adminEndpoint_withTeacherRole_returns403() throws Exception {
        mockMvc.perform(get("/api/internal/admin/users")
                        .header("X-User-Email", "teacher.vana@ou.edu.vn")
                        .header("X-User-Role", "ROLE_TEACHER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("/api/internal/admin/users — ROLE_STUDENT → 403")
    void adminEndpoint_withStudentRole_returns403() throws Exception {
        mockMvc.perform(get("/api/internal/admin/users")
                        .header("X-User-Email", "student.thib@ou.edu.vn")
                        .header("X-User-Role", "ROLE_STUDENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("/api/internal/admin/users — không có header → 401")
    void adminEndpoint_withoutAuthHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/internal/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Internal auth endpoint — /api/internal/auth/** → permitAll
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("/api/internal/auth/{email} — email tồn tại → 200 + AdminUserLoginDTO")
    void internalAuthEndpoint_existingEmail_returns200WithLoginDto() throws Exception {
        mockMvc.perform(get("/api/internal/auth/teacher.vana@ou.edu.vn"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .contains("teacher.vana@ou.edu.vn"));
    }

    @Test
    @DisplayName("/api/internal/auth/{email} — email không tồn tại → 404")
    void internalAuthEndpoint_nonExistentEmail_returns404() throws Exception {
        mockMvc.perform(get("/api/internal/auth/nobody@ou.edu.vn"))
                .andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------------
    // Authenticated user list — /api/secure/users
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("/api/secure/users — authenticated → 200 + danh sách users")
    void secureUsers_authenticated_returns200WithUsers() throws Exception {
        mockMvc.perform(get("/api/secure/users")
                        .header("X-User-Email", "teacher.vana@ou.edu.vn")
                        .header("X-User-Role", "ROLE_TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("/api/secure/users — unauthenticated → 401")
    void secureUsers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/secure/users"))
                .andExpect(status().isUnauthorized());
    }
}
