package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.UserClient;
import com.lqm.admin_service.clients.DeviceClient;
import com.lqm.admin_service.configs.JwtConfig;
import com.lqm.admin_service.dtos.DeviceResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "jwt.secret=mock-secret-key-that-is-at-least-32-bytes-long",
        "jwt.expiration-ms=3600000",
        "spring.cloud.openfeign.circuitbreaker.enabled=false"
})
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceClient deviceClient;

    @MockitoBean
    private com.lqm.admin_service.clients.AttendanceConfigClient attendanceConfigClient;

    @MockitoBean
    private JwtConfig jwtConfig;

    @MockitoBean
    private UserClient userClient;

    @Test
    @DisplayName("GET /devices: returns list view with devices")
    void listDevices_ReturnsView() throws Exception {
        DeviceResponseDTO device = mock(DeviceResponseDTO.class);
        Page<DeviceResponseDTO> page = new PageImpl<>(List.of(device));
        when(deviceClient.getDevices(anyMap())).thenReturn(page);

        mockMvc.perform(get("/devices").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("device/list"))
                .andExpect(model().attributeExists("devices", "params"))
                .andExpect(model().attribute("devices", page));
    }

    @Test
    @DisplayName("GET /devices: handles exception and shows error message")
    void listDevices_ThrowsException_ShowsError() throws Exception {
        when(deviceClient.getDevices(anyMap())).thenThrow(new RuntimeException("Service unavailable"));

        mockMvc.perform(get("/devices"))
                .andExpect(status().isOk())
                .andExpect(view().name("device/list"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "Lỗi: Service unavailable"));
    }

    @Test
    @DisplayName("PATCH /devices/{id}: updates active status and redirects")
    void toggleDeviceActive_Success() throws Exception {
        doNothing().when(deviceClient).updateDeviceActiveStatus(eq("123"), eq(true));

        mockMvc.perform(patch("/devices/123").param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/devices"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("PATCH /devices/{id}: handles exception and redirects with error")
    void toggleDeviceActive_ThrowsException() throws Exception {
        doThrow(new RuntimeException("Update failed")).when(deviceClient).updateDeviceActiveStatus(eq("123"), eq(true));

        mockMvc.perform(patch("/devices/123").param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/devices"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Lỗi: Update failed"));
    }

    @Test
    @DisplayName("DELETE /devices/{id}: deletes device and returns 204")
    void deleteDevice_Returns204() throws Exception {
        doNothing().when(deviceClient).deleteDevice("123");

        mockMvc.perform(delete("/devices/123"))
                .andExpect(status().isNoContent());
    }
}
