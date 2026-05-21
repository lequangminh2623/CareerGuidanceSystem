package com.lqm.score_service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.score_service.BaseIntegrationTest;
import com.lqm.score_service.clients.ClassroomClient;
import com.lqm.score_service.clients.SectionClient;
import com.lqm.score_service.clients.UserClient;
import com.lqm.score_service.dtos.ScoreListRequestDTO;
import com.lqm.score_service.dtos.ScoreRequestDTO;
import com.lqm.score_service.dtos.SectionResponseDTO;
import com.lqm.score_service.dtos.ClassroomDetailsResponseDTO;
import com.lqm.score_service.models.ScoreDetail;
import com.lqm.score_service.repositories.ScoreDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
@DisplayName("AdminTranscriptCrudFlow — Integration Tests")
class AdminTranscriptCrudFlowIT extends BaseIntegrationTest {

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

    private static final String ADMIN_EMAIL = "admin@ou.edu.vn";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private UUID sectionId;
    private UUID studentId;
    private UUID classroomId;

    @BeforeEach
    void setUp() {
        scoreDetailRepository.deleteAll();
        scoreDetailRepository.flush();

        sectionId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        classroomId = UUID.randomUUID();

        // 1. Cấu hình mock để validator kiểm tra học sinh thành công
        when(userClient.checkStudentExistById(any(UUID.class))).thenReturn(true);

        // 2. Mock thông tin học phần
        SectionResponseDTO sectionDto = SectionResponseDTO.builder()
                .id(sectionId)
                .classroomId(classroomId)
                .classroomName("12A1")
                .subjectName("Toan")
                .build();
        when(sectionClient.getSectionResponseById(eq(sectionId))).thenReturn(sectionDto);

        // 3. Mock checks và details cho saveScores
        when(classroomClient.getNonExistingStudentIds(eq(classroomId), ArgumentMatchers.<List<UUID>>any()))
                .thenReturn(List.of());

        ClassroomDetailsResponseDTO classroomDto = ClassroomDetailsResponseDTO.builder()
                .id(classroomId)
                .studentIds(List.of(studentId))
                .build();
        when(classroomClient.getClassroomDetailsResponseById(eq(classroomId))).thenReturn(classroomDto);
    }

    @Test
    @DisplayName("POST & GET /api/internal/admin/transcripts/{sectionId} — Lưu và Lấy bảng điểm")
    void saveAndGetScores_Success() throws Exception {
        // 1. Lưu điểm qua POST API
        ScoreRequestDTO scoreDto = ScoreRequestDTO.builder()
                .studentId(studentId)
                .midtermScore(8.5)
                .finalScore(9.0)
                .extraScores(List.of(7.0, 8.0))
                .build();
        ScoreListRequestDTO request = new ScoreListRequestDTO(List.of(scoreDto));

        mockMvc.perform(post("/api/internal/admin/transcripts/" + sectionId)
                .header("X-User-Email", ADMIN_EMAIL)
                .header("X-User-Role", ADMIN_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Kiểm tra cơ sở dữ liệu đã được cập nhật
        List<ScoreDetail> saved = scoreDetailRepository.findBySectionId(sectionId);
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getStudentId()).isEqualTo(studentId);
        assertThat(saved.get(0).getMidtermScore()).isEqualTo(8.5);
        assertThat(saved.get(0).getFinalScore()).isEqualTo(9.0);

        // 2. Lấy điểm qua GET API
        mockMvc.perform(get("/api/internal/admin/transcripts/" + sectionId)
                .header("X-User-Email", ADMIN_EMAIL)
                .header("X-User-Role", ADMIN_ROLE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(studentId.toString()))
                .andExpect(jsonPath("$[0].midtermScore").value(8.5))
                .andExpect(jsonPath("$[0].finalScore").value(9.0))
                .andExpect(jsonPath("$[0].extraScores[0]").value(7.0))
                .andExpect(jsonPath("$[0].extraScores[1]").value(8.0));
    }
}
