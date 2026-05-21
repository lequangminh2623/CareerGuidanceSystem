package com.lqm.attendance_service.controllers;

import com.lqm.attendance_service.clients.UserClient;
import com.lqm.attendance_service.dtos.AttendanceResponseDTO;
import com.lqm.attendance_service.dtos.UserResponseDTO;
import com.lqm.attendance_service.models.AttendanceStatus;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web Slice Test for {@link ApiAttendanceController}.
 */
@WebMvcTest(ApiAttendanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApiAttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceService attendanceService;

    @MockitoBean
    private UserClient userClient;

    private static final String BASE_URL = "/api/secure/classrooms/{classroomId}/attendances";

    @Nested
    @DisplayName("GET /api/secure/classrooms/{classroomId}/attendances")
    class GetStudentAttendanceByClassroomTests {

        @Test
        @DisplayName("Happy Path: returns list of attendances for current student in a classroom")
        void getStudentAttendance_HappyPath_Returns200WithList() throws Exception {
            // Arrange
            UUID studentId = UUID.randomUUID();
            UUID classroomId = UUID.randomUUID();
            UUID attendanceId = UUID.randomUUID();
            
            UserResponseDTO currentUser = new UserResponseDTO(studentId, "STU001", "John", "Doe");
            AttendanceResponseDTO responseDTO = new AttendanceResponseDTO(
                    attendanceId, studentId, LocalDate.now(), LocalTime.of(8, 0), AttendanceStatus.PRESENT.name()
            );

            given(userClient.getCurrentUser()).willReturn(currentUser);
            given(attendanceService.getStudentAttendanceByClassroom(studentId, classroomId))
                    .willReturn(List.of(responseDTO));

            // Act & Assert
            mockMvc.perform(get(BASE_URL, classroomId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(attendanceId.toString()))
                    .andExpect(jsonPath("$[0].status").value("PRESENT"));
        }
    }
}
