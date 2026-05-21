package com.lqm.academic_service.it;

import com.lqm.academic_service.BaseIntegrationTest;
import com.lqm.academic_service.clients.UserClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test cho {@link com.lqm.academic_service.filters.AuthFilter}.
 *
 * Xác minh Security Filter Chain với header-based authentication:
 * - Endpoint /api/secure/**: cần X-User-Email + X-User-Role
 * - Endpoint /api/internal/admin/**: cần thêm ROLE_ADMIN
 * - Thiếu header → 401; Sai role → 403
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthFilter Security — Integration Tests (academic-service)")
class AuthFilterIT extends BaseIntegrationTest {

    @MockitoBean
    UserClient userClient;

    @Autowired
    MockMvc mockMvc;

    private static final String ADMIN_EMAIL = "admin@ou.edu.vn";
    private static final String TEACHER_EMAIL = "teacher@ou.edu.vn";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static final String TEACHER_ROLE = "ROLE_TEACHER";

    // -----------------------------------------------------------------------
    // Secure endpoint — /api/secure/**
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("/api/secure/sections — không có header → 401")
    void secureEndpoint_withoutHeaders_returns401() throws Exception {
        mockMvc.perform(get("/api/secure/sections"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/api/secure/sections — chỉ có X-User-Email, thiếu X-User-Role → 401")
    void secureEndpoint_withEmailOnly_returns401() throws Exception {
        mockMvc.perform(get("/api/secure/sections")
                        .header("X-User-Email", TEACHER_EMAIL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/api/secure/sections — chỉ có X-User-Role, thiếu X-User-Email → 401")
    void secureEndpoint_withRoleOnly_returns401() throws Exception {
        mockMvc.perform(get("/api/secure/sections")
                        .header("X-User-Role", TEACHER_ROLE))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Admin-only endpoint — /api/internal/admin/**
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("/api/internal/admin/classrooms — không có header → 401")
    void adminEndpoint_withoutHeaders_returns401() throws Exception {
        mockMvc.perform(get("/api/internal/admin/classrooms"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/api/internal/admin/classrooms — ROLE_TEACHER → 403")
    void adminEndpoint_withTeacherRole_returns403() throws Exception {
        mockMvc.perform(get("/api/internal/admin/classrooms")
                        .header("X-User-Email", TEACHER_EMAIL)
                        .header("X-User-Role", TEACHER_ROLE))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("/api/internal/admin/classrooms — ROLE_ADMIN → 200")
    void adminEndpoint_withAdminRole_returns200() throws Exception {
        mockMvc.perform(get("/api/internal/admin/classrooms")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Role", ADMIN_ROLE))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/api/internal/admin/sections/requests — ROLE_ADMIN → 200")
    void adminSectionEndpoint_withAdminRole_returns200() throws Exception {
        mockMvc.perform(get("/api/internal/admin/sections/requests")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Role", ADMIN_ROLE))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/api/internal/admin/sections/requests — ROLE_TEACHER → 403")
    void adminSectionEndpoint_withTeacherRole_returns403() throws Exception {
        mockMvc.perform(get("/api/internal/admin/sections/requests")
                        .header("X-User-Email", TEACHER_EMAIL)
                        .header("X-User-Role", TEACHER_ROLE))
                .andExpect(status().isForbidden());
    }
}
