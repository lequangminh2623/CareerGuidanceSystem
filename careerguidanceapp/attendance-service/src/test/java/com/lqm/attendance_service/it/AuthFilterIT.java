package com.lqm.attendance_service.it;

import com.lqm.attendance_service.BaseIntegrationTest;
import com.lqm.attendance_service.clients.ClassroomClient;
import com.lqm.attendance_service.clients.UserClient;
import com.lqm.attendance_service.services.MqttService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthFilter Security — Integration Tests (attendance-service)")
class AuthFilterIT extends BaseIntegrationTest {

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private ClassroomClient classroomClient;

    @MockitoBean
    private MqttClient mqttClient;

    @MockitoBean
    private MqttService mqttService;

    @Autowired
    private MockMvc mockMvc;

    private static final String ADMIN_EMAIL = "admin@ou.edu.vn";
    private static final String TEACHER_EMAIL = "teacher@ou.edu.vn";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static final String TEACHER_ROLE = "ROLE_TEACHER";

    @Test
    @DisplayName("/api/secure/classrooms/{id}/attendances — không có header → 401")
    void secureEndpoint_withoutHeaders_returns401() throws Exception {
        mockMvc.perform(get("/api/secure/classrooms/00000000-0000-0000-0000-000000000000/attendances"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/api/internal/admin/devices — không có header → 401")
    void adminEndpoint_withoutHeaders_returns401() throws Exception {
        mockMvc.perform(get("/api/internal/admin/devices"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/api/internal/admin/devices — ROLE_TEACHER → 403")
    void adminEndpoint_withTeacherRole_returns403() throws Exception {
        mockMvc.perform(get("/api/internal/admin/devices")
                        .header("X-User-Email", TEACHER_EMAIL)
                        .header("X-User-Role", TEACHER_ROLE))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("/api/internal/admin/devices — ROLE_ADMIN → 200")
    void adminEndpoint_withAdminRole_returns200() throws Exception {
        mockMvc.perform(get("/api/internal/admin/devices")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Role", ADMIN_ROLE))
                .andExpect(status().isOk());
    }
}
