package com.lqm.score_service.controllers;

import com.lqm.score_service.clients.SectionClient;
import com.lqm.score_service.clients.UserClient;
import com.lqm.score_service.dtos.SectionResponseDTO;
import com.lqm.score_service.dtos.StudentScoreResponseDTO;
import com.lqm.score_service.dtos.UserResponseDTO;
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
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiScoreController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ApiScoreController Web MVC Slice Tests")
class ApiScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private UserClient userClient;
    @MockitoBean private ScoreService scoreService;
    @MockitoBean private SectionClient sectionClient;
    @MockitoBean private ScoreMapper scoreMapper;
    @MockitoBean private MessageSource messageSource;

    private static final String BASE_URL = "/api/secure/scores";
    private UUID currentUserId;
    private UUID sectionId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        sectionId = UUID.randomUUID();
        given(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .willAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    @DisplayName("GET /me")
    class GetStudentScoresTests {

        @Test
        @DisplayName("Happy Path: returns 200 with student scores")
        void getStudentScores_HappyPath_Returns200() throws Exception {
            // Arrange
            UserResponseDTO currentUser = new UserResponseDTO(currentUserId, "code", "firstName", "lastName");
            given(userClient.getCurrentUser()).willReturn(currentUser);

            ScoreDetail scoreDetail = ScoreDetail.builder().sectionId(sectionId).studentId(currentUserId).build();
            given(scoreService.getScoreDetails(any(), any())).willReturn(new PageImpl<>(List.of(scoreDetail)));

            SectionResponseDTO sectionDTO = new SectionResponseDTO(sectionId, UUID.randomUUID(), "Giáo viên A", "10A1", "Grade 10", "2024-2025", "First semester", "Toán", "UNLOCKED");
            given(sectionClient.getSections(any(), any())).willReturn(new PageImpl<>(List.of(sectionDTO)));

            StudentScoreResponseDTO responseDTO = new StudentScoreResponseDTO(UUID.randomUUID(), 8.0, 9.0, Collections.emptyList(), "Toán", "10A1", "First semester", "2024-2025");
            given(scoreMapper.toStudentScoreResponseDTO(scoreDetail, sectionDTO)).willReturn(responseDTO);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].subjectName").value("Toán"))
                    .andExpect(jsonPath("$[0].midtermScore").value(8.0));
        }

        @Test
        @DisplayName("Exception Path: User has no student code -> Returns 403")
        void getStudentScores_UserHasNoCode_Returns403() throws Exception {
            // Arrange
            UserResponseDTO currentUser = new UserResponseDTO(currentUserId, null, "firstName", "lastName");
            given(userClient.getCurrentUser()).willReturn(currentUser);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }
}
