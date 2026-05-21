package com.lqm.score_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.score_service.dtos.ScoreListRequestDTO;
import com.lqm.score_service.dtos.ScoreRequestDTO;
import com.lqm.score_service.mappers.ScoreMapper;
import com.lqm.score_service.models.ScoreDetail;
import com.lqm.score_service.services.ScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminTranscriptController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminTranscriptController Web MVC Slice Tests")
class AdminTranscriptControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ScoreService scoreService;
    @MockitoBean private ScoreMapper scoreMapper;

    private static final String BASE_URL = "/api/internal/admin/transcripts";
    private UUID sectionId;
    private UUID studentId;

    @BeforeEach
    void setUp() {
        sectionId = UUID.randomUUID();
        studentId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("GET /{sectionId}")
    class GetScoreRequestsTests {
        @Test
        @DisplayName("Happy Path: returns 200 with list of ScoreRequestDTOs")
        void getScoreRequests_HappyPath_Returns200() throws Exception {
            // Arrange
            ScoreDetail scoreDetail = ScoreDetail.builder().sectionId(sectionId).studentId(studentId).build();
            given(scoreService.getScoreDetails(any(), any())).willReturn(new PageImpl<>(List.of(scoreDetail)));

            ScoreRequestDTO dto = new ScoreRequestDTO();
            dto.setStudentId(studentId);
            given(scoreMapper.toScoreRequestDTO(scoreDetail)).willReturn(dto);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{sectionId}", sectionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].studentId").value(studentId.toString()));
        }
    }

    @Nested
    @DisplayName("POST /{sectionId}")
    class SaveScoresTests {
        @Test
        @DisplayName("Happy Path: successfully saves scores")
        void saveScores_HappyPath_Returns200() throws Exception {
            // Arrange
            ScoreListRequestDTO dto = new ScoreListRequestDTO();
            dto.setScores(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/{sectionId}", sectionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
                    
            verify(scoreService).saveScores(any());
        }
    }
}
