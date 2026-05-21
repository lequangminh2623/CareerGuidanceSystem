package com.lqm.score_service.controllers;

import com.lqm.score_service.services.ScoreService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalTranscriptController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("InternalTranscriptController Web MVC Slice Tests")
class InternalTranscriptControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ScoreService scoreService;

    private static final String BASE_URL = "/api/internal/secure/transcripts";
    private UUID sectionId;

    @BeforeEach
    void setUp() {
        sectionId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("GET /{id}/fully-scored/check")
    class IsTranscriptFullyScoredTests {
        @Test
        @DisplayName("Happy Path: returns 200 with boolean true")
        void isTranscriptFullyScored_ReturnsTrue() throws Exception {
            // Arrange
            given(scoreService.isTranscriptFullyScored(sectionId)).willReturn(true);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{id}/fully-scored/check", sectionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }

        @Test
        @DisplayName("Happy Path: returns 200 with boolean false")
        void isTranscriptFullyScored_ReturnsFalse() throws Exception {
            // Arrange
            given(scoreService.isTranscriptFullyScored(sectionId)).willReturn(false);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{id}/fully-scored/check", sectionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }
    }
}
