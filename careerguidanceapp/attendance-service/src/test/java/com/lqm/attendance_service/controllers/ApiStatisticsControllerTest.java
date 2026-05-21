package com.lqm.attendance_service.controllers;

import com.lqm.attendance_service.clients.UserClient;
import com.lqm.attendance_service.dtos.AttendanceSummaryDTO;
import com.lqm.attendance_service.dtos.UserResponseDTO;
import com.lqm.attendance_service.services.AttendanceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web Slice Test for {@link ApiStatisticsController}.
 */
@WebMvcTest(ApiStatisticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApiStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceService attendanceService;

    @MockitoBean
    private UserClient userClient;

    private static final String BASE_URL = "/api/secure/statistics";

    @Nested
    @DisplayName("GET /api/secure/statistics/attendance")
    class GetAttendanceSummaryTests {

        @Test
        @DisplayName("Happy Path: returns attendance summary for current student")
        void getAttendanceSummary_HappyPath_Returns200WithSummary() throws Exception {
            // Arrange
            UUID studentId = UUID.randomUUID();
            UserResponseDTO currentUser = new UserResponseDTO(studentId, "STU001", "John", "Doe");
            AttendanceSummaryDTO summaryDTO = new AttendanceSummaryDTO(10L, 2L, 0L);

            given(userClient.getCurrentUser()).willReturn(currentUser);
            given(attendanceService.getAttendanceSummary(studentId)).willReturn(summaryDTO);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/attendance"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.presentCount").value(10))
                    .andExpect(jsonPath("$.lateCount").value(2))
                    .andExpect(jsonPath("$.absentCount").value(0));
        }
    }
}
