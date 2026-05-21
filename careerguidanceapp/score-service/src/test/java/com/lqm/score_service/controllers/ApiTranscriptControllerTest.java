package com.lqm.score_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.score_service.clients.ClassroomClient;
import com.lqm.score_service.clients.SectionClient;
import com.lqm.score_service.dtos.ScoreListRequestDTO;
import com.lqm.score_service.dtos.SectionResponseDTO;
import com.lqm.score_service.mappers.ScoreMapper;
import com.lqm.score_service.services.ScoreService;
import com.lqm.score_service.services.SecurityService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@WebMvcTest(ApiTranscriptController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ApiTranscriptController Web MVC Slice Tests")
class ApiTranscriptControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private SectionClient sectionClient;
    @MockitoBean private ScoreService scoreService;
    @MockitoBean private ClassroomClient classroomClient;
    @MockitoBean private MessageSource messageSource;
    @MockitoBean private ScoreMapper scoreMapper;
    @MockitoBean(name = "securityService") private SecurityService securityService;

    private UUID sectionId;
    private static final String BASE_URL = "/api/secure/transcripts";

    @BeforeEach
    void setUp() {
        sectionId = UUID.randomUUID();
        given(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .willAnswer(inv -> inv.getArgument(0));
        // Mock permission to true for all tests to bypass @PreAuthorize in mock environment
        given(securityService.hasPermission(any(), anyString(), anyString())).willReturn(true);
    }

    @Nested
    @DisplayName("GET /{sectionId}/scores")
    class GetTranscriptDetailsTests {
        @Test
        @DisplayName("Happy Path: returns 200 with transcript details")
        void getTranscriptDetails_HappyPath_Returns200() throws Exception {
            // Arrange
            SectionResponseDTO sectionDTO = new SectionResponseDTO(sectionId, UUID.randomUUID(), "Giáo viên A", "10A1", "Grade 10", "2024-2025", "First semester", "Toán", "UNLOCKED");
            given(sectionClient.getSectionResponseById(sectionId)).willReturn(sectionDTO);
            given(scoreService.getScoreDetails(any(), any())).willReturn(new PageImpl<>(Collections.emptyList()));
            given(classroomClient.getStudentsInClassroom(any(), any())).willReturn(new PageImpl<>(Collections.emptyList()));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{sectionId}/scores", sectionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.section.id").value(sectionId.toString()));
        }
    }

    @Nested
    @DisplayName("POST /{sectionId}/scores")
    class SaveScoresTests {

        @Test
        @DisplayName("Happy Path: successfully saves scores when transcript is not locked")
        void saveScores_HappyPath_Returns200() throws Exception {
            // Arrange
            ScoreListRequestDTO dto = new ScoreListRequestDTO();
            dto.setScores(Collections.emptyList());
            
            given(sectionClient.isLockedTranscript(sectionId)).willReturn(false);

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/{sectionId}/scores", sectionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Exception Path: returns 403 Forbidden when transcript is locked")
        void saveScores_TranscriptLocked_Returns403() throws Exception {
            // Arrange
            ScoreListRequestDTO dto = new ScoreListRequestDTO();
            dto.setScores(Collections.emptyList());

            given(sectionClient.isLockedTranscript(sectionId)).willReturn(true);

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/{sectionId}/scores", sectionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /{sectionId}/scores/import")
    class ImportCsvTests {
        @Test
        @DisplayName("Happy Path: returns 200 on successful import")
        void importCsv_HappyPath_Returns200() throws Exception {
            given(sectionClient.isLockedTranscript(sectionId)).willReturn(false);
            MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "content".getBytes());

            mockMvc.perform(multipart(BASE_URL + "/{sectionId}/scores/import", sectionId)
                            .file(file))
                    .andExpect(status().isOk());
        }
        
        @Test
        @DisplayName("Exception Path: returns 403 when transcript is locked")
        void importCsv_LockedTranscript_Returns403() throws Exception {
            given(sectionClient.isLockedTranscript(sectionId)).willReturn(true);
            MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "content".getBytes());

            mockMvc.perform(multipart(BASE_URL + "/{sectionId}/scores/import", sectionId)
                            .file(file))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /{sectionId}/scores/export/csv")
    class ExportCsvTests {
        @Test
        @DisplayName("Happy Path: returns 200 with CSV data when transcript is locked")
        void exportCsv_HappyPath_Returns200() throws Exception {
            given(sectionClient.isLockedTranscript(sectionId)).willReturn(true);
            given(scoreService.generateScoreCsv(sectionId)).willReturn("csv data".getBytes());

            mockMvc.perform(get(BASE_URL + "/{sectionId}/scores/export/csv", sectionId))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                    .andExpect(content().bytes("csv data".getBytes()));
        }

        @Test
        @DisplayName("Exception Path: returns 403 when transcript is not locked")
        void exportCsv_NotLocked_Returns403() throws Exception {
            given(sectionClient.isLockedTranscript(sectionId)).willReturn(false);

            mockMvc.perform(get(BASE_URL + "/{sectionId}/scores/export/csv", sectionId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /{sectionId}/scores/export/pdf")
    class ExportPdfTests {
        @Test
        @DisplayName("Happy Path: returns 200 with PDF data when transcript is locked")
        void exportPdf_HappyPath_Returns200() throws Exception {
            given(sectionClient.isLockedTranscript(sectionId)).willReturn(true);
            given(scoreService.generateScorePdf(sectionId)).willReturn("pdf data".getBytes());

            mockMvc.perform(get(BASE_URL + "/{sectionId}/scores/export/pdf", sectionId))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "application/pdf"))
                    .andExpect(content().bytes("pdf data".getBytes()));
        }

        @Test
        @DisplayName("Exception Path: returns 403 when transcript is not locked")
        void exportPdf_NotLocked_Returns403() throws Exception {
            given(sectionClient.isLockedTranscript(sectionId)).willReturn(false);

            mockMvc.perform(get(BASE_URL + "/{sectionId}/scores/export/pdf", sectionId))
                    .andExpect(status().isForbidden());
        }
    }
}
