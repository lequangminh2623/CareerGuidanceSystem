package com.lqm.attendance_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.attendance_service.dtos.AdminAttendanceRequestDTO;
import com.lqm.attendance_service.dtos.AttendanceListRequestDTO;
import com.lqm.attendance_service.dtos.AttendanceResponseDTO;
import com.lqm.attendance_service.mappers.AttendanceMapper;
import com.lqm.attendance_service.models.Attendance;
import com.lqm.attendance_service.models.AttendanceStatus;
import com.lqm.attendance_service.services.AttendanceService;
import com.lqm.attendance_service.services.FingerprintService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web Slice Test for {@link AdminAttendanceController}.
 */
@WebMvcTest(AdminAttendanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttendanceService attendanceService;

    @MockitoBean
    private AttendanceMapper attendanceMapper;

    @MockitoBean
    private FingerprintService fingerprintService;

    private static final String BASE_URL = "/api/internal/admin/attendances";

    @Nested
    @DisplayName("GET /api/internal/admin/attendances")
    class GetAttendancesTests {

        @Test
        @DisplayName("Happy Path: returns list of attendances by classroom and date")
        void getAttendances_HappyPath_Returns200WithList() throws Exception {
            UUID classroomId = UUID.randomUUID();
            LocalDate attendanceDate = LocalDate.now();
            UUID attendanceId = UUID.randomUUID();
            UUID studentId = UUID.randomUUID();
            
            AttendanceResponseDTO responseDTO = new AttendanceResponseDTO(
                    attendanceId, studentId, attendanceDate, LocalTime.of(8, 0), AttendanceStatus.PRESENT.name()
            );

            given(attendanceService.getAttendancesByClassroomAndDate(classroomId, attendanceDate))
                    .willReturn(List.of(responseDTO));

            mockMvc.perform(get(BASE_URL)
                            .param("classroomId", classroomId.toString())
                            .param("attendanceDate", attendanceDate.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(attendanceId.toString()))
                    .andExpect(jsonPath("$[0].status").value("PRESENT"));
        }
    }

    @Nested
    @DisplayName("POST /api/internal/admin/attendances")
    class SaveAttendancesTests {

        @Test
        @DisplayName("Happy Path: saves attendances and returns 200")
        void saveAttendances_HappyPath_Returns200() throws Exception {
            UUID classroomId = UUID.randomUUID();
            LocalDate attendanceDate = LocalDate.now();
            UUID studentId = UUID.randomUUID();

            AdminAttendanceRequestDTO requestDTO = new AdminAttendanceRequestDTO(studentId, AttendanceStatus.PRESENT.name());
            AttendanceListRequestDTO requestList = new AttendanceListRequestDTO(List.of(requestDTO));
            
            Attendance attendanceEntity = Attendance.builder().studentId(studentId).build();

            given(attendanceMapper.toEntity(any(AdminAttendanceRequestDTO.class), eq(classroomId), eq(attendanceDate)))
                    .willReturn(attendanceEntity);

            mockMvc.perform(post(BASE_URL)
                            .param("classroomId", classroomId.toString())
                            .param("attendanceDate", attendanceDate.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestList)))
                    .andExpect(status().isOk());

            verify(attendanceService).saveAttendances(eq(classroomId), eq(attendanceDate), any());
        }

        @Test
        @DisplayName("Validation Error: returns 400 when missing required fields")
        void saveAttendances_ValidationError_Returns400() throws Exception {
            UUID classroomId = UUID.randomUUID();
            LocalDate attendanceDate = LocalDate.now();

            // Missing studentId and status
            AdminAttendanceRequestDTO invalidRequestDTO = new AdminAttendanceRequestDTO(null, null);
            AttendanceListRequestDTO requestList = new AttendanceListRequestDTO(List.of(invalidRequestDTO));

            mockMvc.perform(post(BASE_URL)
                            .param("classroomId", classroomId.toString())
                            .param("attendanceDate", attendanceDate.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestList)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/internal/admin/attendances")
    class DeleteAttendancesTests {

        @Test
        @DisplayName("Happy Path: deletes attendances by classroom and date")
        void deleteAttendances_HappyPath_Returns200() throws Exception {
            UUID classroomId = UUID.randomUUID();
            LocalDate attendanceDate = LocalDate.now();

            mockMvc.perform(delete(BASE_URL)
                            .param("classroomId", classroomId.toString())
                            .param("attendanceDate", attendanceDate.toString()))
                    .andExpect(status().isOk());

            verify(attendanceService).deleteAttendancesByClassroomAndDate(classroomId, attendanceDate);
        }
    }

    @Nested
    @DisplayName("DELETE /api/internal/admin/attendances/classrooms/{classroomId}")
    class DeleteAttendancesForClassroomTests {

        @Test
        @DisplayName("Happy Path: deletes attendances and fingerprints for specific students")
        void deleteAttendancesForClassroom_HappyPath_Returns200() throws Exception {
            UUID classroomId = UUID.randomUUID();
            List<UUID> studentIds = List.of(UUID.randomUUID(), UUID.randomUUID());

            mockMvc.perform(delete(BASE_URL + "/classrooms/{classroomId}", classroomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(studentIds)))
                    .andExpect(status().isOk());

            verify(attendanceService).deleteAttendancesByClassroomAndStudentIds(classroomId, studentIds);
            verify(fingerprintService).deleteFingerprintsByClassroomAndStudentIds(classroomId, studentIds);
        }
    }
}
