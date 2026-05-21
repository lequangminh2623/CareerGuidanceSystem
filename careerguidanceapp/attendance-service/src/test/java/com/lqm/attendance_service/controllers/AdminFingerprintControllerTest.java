package com.lqm.attendance_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.attendance_service.dtos.FingerprintRequestDTO;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.models.Fingerprint;
import com.lqm.attendance_service.repositories.FingerprintRepository;
import com.lqm.attendance_service.services.DeviceService;
import com.lqm.attendance_service.services.MqttService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web Slice Test for {@link AdminFingerprintController}.
 */
@WebMvcTest(AdminFingerprintController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminFingerprintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MqttService mqttService;

    @MockitoBean
    private DeviceService deviceService;

    @MockitoBean
    private FingerprintRepository fingerprintRepository;

    @MockitoBean
    private MessageSource messageSource;

    private static final String BASE_URL = "/api/internal/admin/fingerprints";

    @Nested
    @DisplayName("POST /api/internal/admin/fingerprints/enroll")
    class EnrollFingerprintTests {

        @Test
        @DisplayName("Happy Path: enrolls fingerprint successfully when device is active")
        void enrollFingerprint_HappyPath_Returns200() throws Exception {
            UUID classroomId = UUID.randomUUID();
            UUID studentId = UUID.randomUUID();
            FingerprintRequestDTO requestDTO = new FingerprintRequestDTO(null, classroomId, studentId, "John Doe");

            Device device = Device.builder().id("ESP32_001").isActive(true).build();
            Fingerprint existingFingerprint = Fingerprint.builder().fingerprintIndex(5).build();

            given(deviceService.getDeviceByClassroom(classroomId)).willReturn(device);
            given(fingerprintRepository.findByStudentIdAndClassroomId(studentId, classroomId))
                    .willReturn(Optional.of(existingFingerprint));

            mockMvc.perform(post(BASE_URL + "/enroll")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk());

            verify(mqttService).startEnrollment(eq("ESP32_001"), any(FingerprintRequestDTO.class));
        }

        @Test
        @DisplayName("Validation Error: returns 400 when device is inactive")
        void enrollFingerprint_DeviceInactive_Returns400() throws Exception {
            UUID classroomId = UUID.randomUUID();
            UUID studentId = UUID.randomUUID();
            FingerprintRequestDTO requestDTO = new FingerprintRequestDTO(null, classroomId, studentId, "John Doe");

            Device device = Device.builder().id("ESP32_001").isActive(false).build();

            given(deviceService.getDeviceByClassroom(classroomId)).willReturn(device);
            given(messageSource.getMessage(eq("device.deactive"), any(), any())).willReturn("Device is inactive");

            mockMvc.perform(post(BASE_URL + "/enroll")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/internal/admin/fingerprints/cancel")
    class CancelEnrollmentTests {

        @Test
        @DisplayName("Happy Path: cancels enrollment when device is active")
        void cancelEnrollment_HappyPath_Returns200() throws Exception {
            UUID classroomId = UUID.randomUUID();
            Device device = Device.builder().id("ESP32_001").isActive(true).build();

            given(deviceService.getDeviceByClassroom(classroomId)).willReturn(device);

            mockMvc.perform(post(BASE_URL + "/cancel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(classroomId)))
                    .andExpect(status().isOk());

            verify(mqttService).cancelEnrollment("ESP32_001");
        }
    }
}
