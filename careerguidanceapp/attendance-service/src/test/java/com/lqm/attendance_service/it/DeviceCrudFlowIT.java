package com.lqm.attendance_service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.attendance_service.BaseIntegrationTest;
import com.lqm.attendance_service.clients.ClassroomClient;
import com.lqm.attendance_service.clients.UserClient;
import com.lqm.attendance_service.dtos.AcademicResponseDTO;
import com.lqm.attendance_service.dtos.DeviceRequestDTO;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.repositories.DeviceRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("DeviceCrudFlow — Integration Tests")
class DeviceCrudFlowIT extends BaseIntegrationTest {

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
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ADMIN_EMAIL = "admin@ou.edu.vn";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private UUID classroomId;
    private String deviceId;

    @BeforeEach
    void setUp() {
        deviceRepository.deleteAll();
        deviceRepository.flush();

        classroomId = UUID.randomUUID();
        deviceId = "123456ABCDEF";
    }

    @Test
    @DisplayName("POST /api/internal/admin/devices/assignments — Gán thiết bị cho lớp")
    void assignDeviceToClassroom_Success() throws Exception {
        // Save device in database first
        Device dev = Device.builder()
                .id(deviceId)
                .isActive(true)
                .build();
        deviceRepository.save(dev);
        deviceRepository.flush();

        DeviceRequestDTO request = new DeviceRequestDTO(deviceId, classroomId);

        mockMvc.perform(post("/api/internal/admin/devices/assignments")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Role", ADMIN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        assertThat(deviceOpt).isPresent();
        assertThat(deviceOpt.get().getClassroomId()).isEqualTo(classroomId);
        assertThat(deviceOpt.get().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("GET /api/internal/admin/devices — Lấy danh sách devices với phân trang")
    void listDevices_ReturnsPagedDevices() throws Exception {
        Device dev = Device.builder()
                .id(deviceId)
                .classroomId(classroomId)
                .isActive(true)
                .build();
        deviceRepository.save(dev);
        deviceRepository.flush();

        // Mock classroomName từ classroomClient
        AcademicResponseDTO acadDTO = new AcademicResponseDTO(classroomId, "10A1");
        when(classroomClient.getClassroomDetailNames(eq(List.of(classroomId)), any()))
                .thenReturn(List.of(acadDTO));

        mockMvc.perform(get("/api/internal/admin/devices")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Role", ADMIN_ROLE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(deviceId))
                .andExpect(jsonPath("$.content[0].classroomName").value("10A1"));
    }

    @Test
    @DisplayName("PATCH /api/internal/admin/devices/{id} — Cập nhật trạng thái active")
    void updateDeviceActiveStatus_Success() throws Exception {
        Device dev = Device.builder()
                .id(deviceId)
                .classroomId(classroomId)
                .isActive(true)
                .build();
        deviceRepository.save(dev);
        deviceRepository.flush();

        mockMvc.perform(patch("/api/internal/admin/devices/" + deviceId)
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Role", ADMIN_ROLE)
                        .param("active", "false"))
                .andExpect(status().isOk());

        Device updated = deviceRepository.findById(deviceId).orElseThrow();
        assertThat(updated.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("DELETE /api/internal/admin/devices/{id} — Xoá thiết bị")
    void deleteDevice_Success() throws Exception {
        Device dev = Device.builder()
                .id(deviceId)
                .classroomId(classroomId)
                .isActive(true)
                .build();
        deviceRepository.save(dev);
        deviceRepository.flush();

        mockMvc.perform(delete("/api/internal/admin/devices/" + deviceId)
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Role", ADMIN_ROLE))
                .andExpect(status().isNoContent());

        assertThat(deviceRepository.findById(deviceId)).isEmpty();
    }
}
