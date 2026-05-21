package com.lqm.attendance_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.attendance_service.dtos.DeviceRequestDTO;
import com.lqm.attendance_service.dtos.DeviceResponseDTO;
import com.lqm.attendance_service.dtos.DeviceStatusDTO;
import com.lqm.attendance_service.mappers.DeviceMapper;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.services.DeviceService;
import com.lqm.attendance_service.utils.PageableUtil;
import com.lqm.attendance_service.validators.WebAppValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web Slice Test for {@link AdminDeviceController}.
 */
@WebMvcTest(AdminDeviceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminDeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeviceService deviceService;

    @MockitoBean
    private PageableUtil pageableUtil;

    @MockitoBean
    private DeviceMapper deviceMapper;

    @MockitoBean
    private WebAppValidator webAppValidator;

    private static final String BASE_URL = "/api/internal/admin/devices";

    @Nested
    @DisplayName("GET /api/internal/admin/devices")
    class ListDevicesTests {

        @Test
        @DisplayName("Happy Path: returns paginated list of devices")
        void listDevices_HappyPath_Returns200WithPage() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Device device = Device.builder().id("ESP32_001").isActive(true).build();
            Page<Device> devicePage = new PageImpl<>(List.of(device), pageable, 1);
            DeviceResponseDTO responseDTO = new DeviceResponseDTO("ESP32_001", true, "10A1");

            given(pageableUtil.getPageable(anyString(), anyInt(), any())).willReturn(pageable);
            given(deviceService.getAllDevices(anyMap(), eq(pageable))).willReturn(devicePage);
            given(deviceService.buildClassroomMap(any())).willReturn(Map.of(UUID.randomUUID(), "10A1"));
            given(deviceMapper.toDeviceResponseDTO(eq(device), anyMap())).willReturn(responseDTO);

            mockMvc.perform(get(BASE_URL)
                            .param("page", "1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value("ESP32_001"))
                    .andExpect(jsonPath("$.content[0].isActive").value(true));
        }
    }

    @Nested
    @DisplayName("PATCH /api/internal/admin/devices/{id}")
    class UpdateDeviceActiveStatusTests {

        @Test
        @DisplayName("Happy Path: updates device status")
        void updateDeviceActiveStatus_HappyPath_Returns200() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/ESP32_001")
                            .param("active", "false"))
                    .andExpect(status().isOk());

            verify(deviceService).updateDeviceActiveStatus("ESP32_001", false, true);
        }
    }

    @Nested
    @DisplayName("DELETE /api/internal/admin/devices/{id}")
    class DeleteDeviceTests {

        @Test
        @DisplayName("Happy Path: deletes device")
        void deleteDevice_HappyPath_Returns204() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/ESP32_001"))
                    .andExpect(status().isNoContent());

            verify(deviceService).deleteDevice("ESP32_001");
        }
    }

    @Nested
    @DisplayName("GET /api/internal/admin/devices/available")
    class GetAvailableDevicesTests {

        @Test
        @DisplayName("Happy Path: returns list of unassigned devices")
        void getAvailableDevices_HappyPath_Returns200WithList() throws Exception {
            Device device = Device.builder().id("ESP32_002").isActive(true).build();
            DeviceStatusDTO dto = new DeviceStatusDTO("ESP32_002", true);

            given(deviceService.getDevicesWithoutClassroom()).willReturn(List.of(device));
            given(deviceMapper.toDeviceStatusDTO(device)).willReturn(dto);

            mockMvc.perform(get(BASE_URL + "/available"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value("ESP32_002"));
        }
    }

    @Nested
    @DisplayName("GET /api/internal/admin/devices/classrooms/{classroomId}")
    class GetDeviceByClassroomTests {

        @Test
        @DisplayName("Happy Path: returns device for classroom")
        void getDeviceByClassroom_HappyPath_Returns200() throws Exception {
            UUID classroomId = UUID.randomUUID();
            Device device = Device.builder().id("ESP32_003").classroomId(classroomId).isActive(true).build();
            DeviceStatusDTO dto = new DeviceStatusDTO("ESP32_003", true);

            given(deviceService.getDeviceByClassroom(classroomId)).willReturn(device);
            given(deviceMapper.toDeviceStatusDTO(device)).willReturn(dto);

            mockMvc.perform(get(BASE_URL + "/classrooms/{classroomId}", classroomId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value("ESP32_003"));
        }
    }

    @Nested
    @DisplayName("POST /api/internal/admin/devices/assignments")
    class AssignDeviceToClassroomTests {

        @Test
        @DisplayName("Happy Path: assigns device")
        void assignDeviceToClassroom_HappyPath_Returns200() throws Exception {
            UUID classroomId = UUID.randomUUID();
            DeviceRequestDTO requestDTO = new DeviceRequestDTO("A1B2C3D4E5F6", classroomId);
            Device device = Device.builder().id("A1B2C3D4E5F6").classroomId(classroomId).build();

            // We mock the validator to do nothing
            given(webAppValidator.supports(any())).willReturn(true);
            given(deviceMapper.toEntity(any(DeviceRequestDTO.class))).willReturn(device);

            mockMvc.perform(post(BASE_URL + "/assignments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk());

            verify(deviceService).assignDeviceToClassroom(device);
        }
    }

    @Nested
    @DisplayName("POST /api/internal/admin/devices/{deviceId}/unassignment")
    class UnassignDeviceFromClassroomTests {

        @Test
        @DisplayName("Happy Path: unassigns device")
        void unassignDeviceFromClassroom_HappyPath_Returns200() throws Exception {
            mockMvc.perform(post(BASE_URL + "/ESP32_005/unassignment"))
                    .andExpect(status().isOk());

            verify(deviceService).unassignDeviceFromClassroom("ESP32_005");
        }
    }

    @Nested
    @DisplayName("POST /api/internal/admin/devices/classrooms/{classroomId}/unassignment")
    class UnassignDeviceByClassroomIdTests {

        @Test
        @DisplayName("Happy Path: unassigns device by classroom")
        void unassignDeviceByClassroomId_HappyPath_Returns200() throws Exception {
            UUID classroomId = UUID.randomUUID();

            mockMvc.perform(post(BASE_URL + "/classrooms/{classroomId}/unassignment", classroomId))
                    .andExpect(status().isOk());

            verify(deviceService).unassignDeviceByClassroomId(classroomId);
        }
    }
}
