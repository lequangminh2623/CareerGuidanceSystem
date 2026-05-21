package com.lqm.admin_service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.admin_service.clients.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("EnrollSseControllerIT — Integration Tests")
class EnrollSseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthClient authClient;
    @MockitoBean
    private DeviceClient deviceClient;
    @MockitoBean
    private SectionClient sectionClient;
    @MockitoBean
    private ClassroomClient classroomClient;
    @MockitoBean
    private UserClient userClient;
    @MockitoBean
    private SubjectClient subjectClient;
    @MockitoBean
    private FingerprintClient fingerprintClient;
    @MockitoBean
    private AttendanceClient attendanceClient;
    @MockitoBean
    private SemesterClient semesterClient;
    @MockitoBean
    private CurriculumClient curriculumClient;
    @MockitoBean
    private GradeClient gradeClient;
    @MockitoBean
    private TranscriptClient transcriptClient;
    @MockitoBean
    private YearClient yearClient;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /sse — Mở kết nối SSE thành công")
    void subscribeSse_Success() throws Exception {
        UUID classroomId = UUID.randomUUID();

        mockMvc.perform(get("/classrooms/" + classroomId + "/fingerprints/enroll/sse"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());
    }

    @Test
    @DisplayName("POST /internal/enroll-result — Nhận kết quả enroll thành công")
    void receiveEnrollResult_Success() throws Exception {
        UUID classroomId = UUID.randomUUID();
        Map<String, Object> resultPayload = Map.of("success", true, "message", "Enroll OK");

        mockMvc.perform(post("/internal/enroll-result/" + classroomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resultPayload))
                .with(csrf())) // Internal API might require CSRF if not explicitly ignored, though usually it
                               // is. We pass it just in case.
                .andExpect(status().isOk());
    }
}
