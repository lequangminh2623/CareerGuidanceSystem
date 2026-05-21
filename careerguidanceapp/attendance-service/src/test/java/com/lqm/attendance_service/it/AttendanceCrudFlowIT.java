package com.lqm.attendance_service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.attendance_service.BaseIntegrationTest;
import com.lqm.attendance_service.clients.ClassroomClient;
import com.lqm.attendance_service.clients.UserClient;
import com.lqm.attendance_service.dtos.AdminAttendanceRequestDTO;
import com.lqm.attendance_service.dtos.AttendanceListRequestDTO;
import com.lqm.attendance_service.models.Attendance;
import com.lqm.attendance_service.models.AttendanceStatus;
import com.lqm.attendance_service.models.Fingerprint;
import com.lqm.attendance_service.repositories.AttendanceRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AttendanceCrudFlow — Integration Tests")
class AttendanceCrudFlowIT extends BaseIntegrationTest {

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private ClassroomClient classroomClient;

    @MockitoBean
    private MqttClient mqttClient;

    @MockitoBean
    private MqttService mqttService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private FingerprintRepository fingerprintRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ADMIN_EMAIL = "admin@ou.edu.vn";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private UUID classroomId;
    private UUID student1;
    private UUID student2;
    private LocalDate attendanceDate;

    @BeforeEach
    void setUp() {
        attendanceRepository.deleteAll();
        attendanceRepository.flush();
        fingerprintRepository.deleteAll();
        fingerprintRepository.flush();

        classroomId = UUID.randomUUID();
        student1 = UUID.randomUUID();
        student2 = UUID.randomUUID();
        attendanceDate = LocalDate.now();
    }

    @Test
    @DisplayName("GET /api/internal/admin/attendances — Lấy danh sách điểm danh theo lớp và ngày")
    void getAttendances_Success() throws Exception {
        Attendance att = Attendance.builder()
                .classroomId(classroomId)
                .studentId(student1)
                .attendanceDate(attendanceDate)
                .status(AttendanceStatus.PRESENT)
                .build();
        attendanceRepository.save(att);
        attendanceRepository.flush();

        mockMvc.perform(get("/api/internal/admin/attendances")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Role", ADMIN_ROLE)
                        .param("classroomId", classroomId.toString())
                        .param("attendanceDate", attendanceDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(student1.toString()))
                .andExpect(jsonPath("$[0].status").value("Present"));
    }

    @Test
    @DisplayName("POST /api/internal/admin/attendances — Lưu danh sách điểm danh")
    void saveAttendances_Success() throws Exception {
        AdminAttendanceRequestDTO req1 = new AdminAttendanceRequestDTO(student1, AttendanceStatus.PRESENT.name());
        AdminAttendanceRequestDTO req2 = new AdminAttendanceRequestDTO(student2, AttendanceStatus.ABSENT.name());
        AttendanceListRequestDTO requestList = new AttendanceListRequestDTO(List.of(req1, req2));

        mockMvc.perform(post("/api/internal/admin/attendances")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Role", ADMIN_ROLE)
                        .param("classroomId", classroomId.toString())
                        .param("attendanceDate", attendanceDate.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestList)))
                .andExpect(status().isOk());

        List<Attendance> saved = attendanceRepository.findByClassroomIdAndAttendanceDate(classroomId, attendanceDate);
        assertThat(saved).hasSize(2);
        assertThat(saved).extracting(Attendance::getStudentId).containsExactlyInAnyOrder(student1, student2);
    }

    @Test
    @DisplayName("DELETE /api/internal/admin/attendances — Xoá điểm danh theo lớp và ngày")
    void deleteAttendances_Success() throws Exception {
        Attendance att = Attendance.builder()
                .classroomId(classroomId)
                .studentId(student1)
                .attendanceDate(attendanceDate)
                .status(AttendanceStatus.PRESENT)
                .build();
        attendanceRepository.save(att);
        attendanceRepository.flush();

        mockMvc.perform(delete("/api/internal/admin/attendances")
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Role", ADMIN_ROLE)
                        .param("classroomId", classroomId.toString())
                        .param("attendanceDate", attendanceDate.toString()))
                .andExpect(status().isOk());

        assertThat(attendanceRepository.findByClassroomIdAndAttendanceDate(classroomId, attendanceDate)).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/internal/admin/attendances/classrooms/{classroomId} — Xoá điểm danh và vân tay của các student trong lớp")
    void deleteAttendancesForClassroom_Success() throws Exception {
        Attendance att = Attendance.builder()
                .classroomId(classroomId)
                .studentId(student1)
                .attendanceDate(attendanceDate)
                .status(AttendanceStatus.PRESENT)
                .build();
        attendanceRepository.save(att);
        attendanceRepository.flush();

        Fingerprint fp = Fingerprint.builder()
                .fingerprintIndex(1)
                .classroomId(classroomId)
                .studentId(student1)
                .build();
        fingerprintRepository.save(fp);
        fingerprintRepository.flush();

        mockMvc.perform(delete("/api/internal/admin/attendances/classrooms/" + classroomId)
                        .header("X-User-Email", ADMIN_EMAIL)
                        .header("X-User-Role", ADMIN_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(student1))))
                .andExpect(status().isOk());

        assertThat(attendanceRepository.findByStudentId(student1)).isEmpty();
        assertThat(fingerprintRepository.findByStudentIdAndClassroomId(student1, classroomId)).isEmpty();
    }
}
