package com.lqm.score_service.it;

import com.lqm.score_service.BaseIntegrationTest;
import com.lqm.score_service.clients.ClassroomClient;
import com.lqm.score_service.clients.SectionClient;
import com.lqm.score_service.clients.UserClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthFilter Security — Integration Tests (score-service)")
class AuthFilterIT extends BaseIntegrationTest {

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private ClassroomClient classroomClient;

    @MockitoBean
    private SectionClient sectionClient;

    @Autowired
    private MockMvc mockMvc;

    private static final String ADMIN_EMAIL = "admin@ou.edu.vn";
    private static final String TEACHER_EMAIL = "teacher@ou.edu.vn";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static final String TEACHER_ROLE = "ROLE_TEACHER";

    @Test
    @DisplayName("/api/internal/admin/transcripts/{id} — không có header → 401")
    void secureEndpoint_withoutHeaders_returns401() throws Exception {
        UUID sectionId = UUID.randomUUID();
        mockMvc.perform(get("/api/internal/admin/transcripts/" + sectionId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/api/internal/admin/transcripts/{id} — ROLE_TEACHER → 403")
    void secureEndpoint_withTeacherRole_returns403() throws Exception {
        UUID sectionId = UUID.randomUUID();
        mockMvc.perform(get("/api/internal/admin/transcripts/" + sectionId)
                        .header("X-User-Email", TEACHER_EMAIL)
                        .header("X-User-Role", TEACHER_ROLE))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("/api/internal/admin/transcripts/{id} — ROLE_ADMIN → 200")
    void secureEndpoint_withAdminRole_returns200() throws Exception {
        UUID sectionId = UUID.randomUUID();
        mockMvc.perform(get("/api/internal/admin/transcripts/" + sectionId)
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Role", ADMIN_ROLE))
                .andExpect(status().isOk());
    }
}
