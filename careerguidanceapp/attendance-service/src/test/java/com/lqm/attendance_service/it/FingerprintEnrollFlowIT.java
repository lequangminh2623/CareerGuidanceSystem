package com.lqm.attendance_service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.attendance_service.BaseIntegrationTest;
import com.lqm.attendance_service.clients.ClassroomClient;
import com.lqm.attendance_service.clients.UserClient;
import com.lqm.attendance_service.dtos.FingerprintRequestDTO;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.repositories.DeviceRepository;
import com.lqm.attendance_service.repositories.FingerprintRepository;
import com.lqm.attendance_service.services.MqttService;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("FingerprintEnrollFlow — Integration Tests")
class FingerprintEnrollFlowIT extends BaseIntegrationTest {

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private ClassroomClient classroomClient;

    @MockitoBean
    private MqttClient mqttClient;

    @MockitoBean
    private MqttService mqttService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private FingerprintRepository fingerprintRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ADMIN_EMAIL = "admin@ou.edu.vn";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private UUID classroomId;
    private String deviceId;
    private UUID studentId;

    @BeforeEach
    void setUp() {
        fingerprintRepository.deleteAll();
        deviceRepository.deleteAll();
        deviceRepository.flush();

        classroomId = UUID.randomUUID();
        deviceId = "A1B2C3D4E5F6";
        studentId = UUID.randomUUID();
    }

    @Test
    @DisplayName("POST /api/internal/admin/fingerprints/enroll — Bắt đầu quá trình enroll khi thiết bị hoạt động")
    void enrollFingerprint_Success() throws Exception {
        // Gán thiết bị active
        Device device = Device.builder()
                .id(deviceId)
                .classroomId(classroomId)
                .isActive(true)
                .build();
        deviceRepository.save(device);
        deviceRepository.flush();

        FingerprintRequestDTO request = new FingerprintRequestDTO(null, classroomId, studentId, "Nguyen Van A");

        mockMvc.perform(post("/api/internal/admin/fingerprints/enroll")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Role", ADMIN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(mqttService).startEnrollment(eq(deviceId), any(FingerprintRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/internal/admin/fingerprints/enroll — Trả về 400 nếu thiết bị không hoạt động")
    void enrollFingerprint_DeviceInactive_Returns400() throws Exception {
        // Gán thiết bị inactive
        Device device = Device.builder()
                .id(deviceId)
                .classroomId(classroomId)
                .isActive(false)
                .build();
        deviceRepository.save(device);
        deviceRepository.flush();

        FingerprintRequestDTO request = new FingerprintRequestDTO(null, classroomId, studentId, "Nguyen Van A");

        mockMvc.perform(post("/api/internal/admin/fingerprints/enroll")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Role", ADMIN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/internal/admin/fingerprints/cancel — Huỷ enrollment")
    void cancelEnrollment_Success() throws Exception {
        Device device = Device.builder()
                .id(deviceId)
                .classroomId(classroomId)
                .isActive(true)
                .build();
        deviceRepository.save(device);
        deviceRepository.flush();

        mockMvc.perform(post("/api/internal/admin/fingerprints/cancel")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Role", ADMIN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classroomId)))
                .andExpect(status().isOk());

        verify(mqttService).cancelEnrollment(deviceId);
    }
}
