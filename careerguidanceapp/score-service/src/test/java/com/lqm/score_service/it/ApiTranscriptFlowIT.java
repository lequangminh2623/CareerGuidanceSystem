package com.lqm.score_service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.score_service.BaseIntegrationTest;
import com.lqm.score_service.clients.ClassroomClient;
import com.lqm.score_service.clients.SectionClient;
import com.lqm.score_service.clients.UserClient;
import com.lqm.score_service.dtos.ScoreListRequestDTO;
import com.lqm.score_service.dtos.ScoreRequestDTO;
import com.lqm.score_service.dtos.SectionResponseDTO;
import com.lqm.score_service.dtos.UserResponseDTO;
import com.lqm.score_service.models.ScoreDetail;
import com.lqm.score_service.repositories.ScoreDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ApiTranscriptFlow — Integration Tests")
class ApiTranscriptFlowIT extends BaseIntegrationTest {

        @MockitoBean
        private UserClient userClient;

        @MockitoBean
        private ClassroomClient classroomClient;

        @MockitoBean
        private SectionClient sectionClient;

        @Autowired
        private ScoreDetailRepository scoreDetailRepository;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private static final String TEACHER_EMAIL = "teacher@ou.edu.vn";
        private static final String TEACHER_ROLE = "ROLE_TEACHER";

        private UUID sectionId;
        private UUID classroomId;
        private UUID teacherId;
        private UUID studentId;

        @BeforeEach
        void setUp() {
                scoreDetailRepository.deleteAll();
                scoreDetailRepository.flush();

                sectionId = UUID.randomUUID();
                classroomId = UUID.randomUUID();
                teacherId = UUID.randomUUID();
                studentId = UUID.randomUUID();

                // 1. Mock user hiện tại là giáo viên
                UserResponseDTO currentTeacher = UserResponseDTO.builder()
                                .id(teacherId)
                                .firstName("Nguyen")
                                .lastName("Van Giao Vien")
                                .build();
                when(userClient.getCurrentUser()).thenReturn(currentTeacher);

                // 2. Mock quyền hạn cho giáo viên truy cập lớp/học phần
                when(sectionClient.checkTeacherPermission(eq(sectionId), eq(teacherId))).thenReturn(true);
                when(sectionClient.isLockedTranscript(eq(sectionId))).thenReturn(false);

                // 3. Mock thông tin học phần
                SectionResponseDTO sectionDto = SectionResponseDTO.builder()
                                .id(sectionId)
                                .classroomId(classroomId)
                                .classroomName("12A1")
                                .subjectName("Toan")
                                .build();
                when(sectionClient.getSectionResponseById(eq(sectionId))).thenReturn(sectionDto);

                // 4. Mock học sinh tồn tại
                when(userClient.checkStudentExistById(eq(studentId))).thenReturn(true);

                // 5. Mock danh sách học sinh trong lớp học phần
                UserResponseDTO studentDto = UserResponseDTO.builder()
                                .id(studentId)
                                .code("STU001")
                                .firstName("Nguyen")
                                .lastName("Van Hoc Sinh")
                                .build();
                when(classroomClient.getStudentsInClassroom(eq(classroomId),
                                ArgumentMatchers.<Map<String, String>>any()))

                                .thenReturn(new PageImpl<>(List.of(studentDto)));

                // 6. Mock checks và details cho saveScores
                when(classroomClient.getNonExistingStudentIds(eq(classroomId),
                                org.mockito.ArgumentMatchers.<List<UUID>>any()))
                                .thenReturn(List.of());

                com.lqm.score_service.dtos.ClassroomDetailsResponseDTO classroomDto = com.lqm.score_service.dtos.ClassroomDetailsResponseDTO
                                .builder()
                                .id(classroomId)
                                .studentIds(List.of(studentId))
                                .build();
                when(classroomClient.getClassroomDetailsResponseById(eq(classroomId)))
                                .thenReturn(classroomDto);
        }

        @Test
        @DisplayName("POST & GET /api/secure/transcripts/{sectionId}/scores — Giáo viên lưu và lấy danh sách điểm")
        void saveAndGetScoresByTeacher_Success() throws Exception {
                // 1. Lưu điểm bằng POST API
                ScoreRequestDTO scoreDto = ScoreRequestDTO.builder()
                                .studentId(studentId)
                                .midtermScore(7.5)
                                .finalScore(8.0)
                                .extraScores(List.of(9.0))
                                .build();
                ScoreListRequestDTO requestList = new ScoreListRequestDTO(List.of(scoreDto));

                mockMvc.perform(post("/api/secure/transcripts/" + sectionId + "/scores")
                                .header("X-User-Email", TEACHER_EMAIL)
                                .header("X-User-Role", TEACHER_ROLE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestList)))
                                .andExpect(status().isOk());

                // Kiểm tra cơ sở dữ liệu
                List<ScoreDetail> saved = scoreDetailRepository.findBySectionId(sectionId);
                assertThat(saved).hasSize(1);
                assertThat(saved.get(0).getMidtermScore()).isEqualTo(7.5);
                assertThat(saved.get(0).getFinalScore()).isEqualTo(8.0);

                // 2. Lấy điểm chi tiết bằng GET API
                mockMvc.perform(get("/api/secure/transcripts/" + sectionId + "/scores")
                                .header("X-User-Email", TEACHER_EMAIL)
                                .header("X-User-Role", TEACHER_ROLE))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.section.classroomName").value("12A1"))
                                .andExpect(jsonPath("$.scores[0].studentId").value(studentId.toString()))
                                .andExpect(jsonPath("$.scores[0].midtermScore").value(7.5))
                                .andExpect(jsonPath("$.scores[0].finalScore").value(8.0))
                                .andExpect(jsonPath("$.students['" + studentId + "'].code").value("STU001"));
        }
}
