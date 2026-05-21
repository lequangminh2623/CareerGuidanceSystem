package com.lqm.academic_service.controllers;

import com.lqm.academic_service.clients.UserClient;
import com.lqm.academic_service.dtos.SectionResponseDTO;
import com.lqm.academic_service.dtos.UserResponseDTO;
import com.lqm.academic_service.mappers.SectionMapper;
import com.lqm.academic_service.models.Section;
import com.lqm.academic_service.services.SectionService;
import com.lqm.academic_service.utils.PageableUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web Slice Test for {@link ApiSectionController}.
 */
@WebMvcTest(ApiSectionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApiSectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserClient userClient;
    @MockitoBean
    private SectionService sectionService;
    @MockitoBean
    private PageableUtil pageableUtil;
    @MockitoBean
    private SectionMapper sectionMapper;
    @MockitoBean
    private MessageSource messageSource;

    private static final String BASE_URL = "/api/secure/sections";

    @Nested
    @DisplayName("GET /")
    class GetTranscriptsTests {

        @Test
        @DisplayName("Happy Path: returns paginated transcripts for current teacher")
        void getTranscripts_HappyPath_Returns200WithBody() throws Exception {
            // Arrange
            UUID teacherId = UUID.randomUUID();
            UserResponseDTO teacher = UserResponseDTO.builder().id(teacherId).build();
            Section section = new Section();
            SectionResponseDTO responseDTO = SectionResponseDTO.builder().id(UUID.randomUUID()).build();

            given(userClient.getCurrentUser()).willReturn(teacher);
            given(pageableUtil.getPageable(anyString(), anyInt(), anyList())).willReturn(Pageable.ofSize(10));
            given(sectionService.getSections(anyMap(), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(section)));
            given(sectionService.buildTeacherMap(anyList())).willReturn(Map.of());
            given(sectionMapper.toSectionResponseDTO(eq(section), anyMap())).willReturn(responseDTO);

            // Act & Assert
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transcripts.content", hasSize(1)))
                    .andExpect(jsonPath("$.transcripts.content[0].id").value(responseDTO.id().toString()));
        }
    }

    @Nested
    @DisplayName("PATCH /{sectionId}/lock")
    class LockTranscriptTests {

        @Test
        @DisplayName("Happy Path: locks transcript and returns success message")
        void lockTranscript_HappyPath_Returns200() throws Exception {
            // Arrange
            UUID sectionId = UUID.randomUUID();
            given(messageSource.getMessage(eq("success"), isNull(), any(Locale.class))).willReturn("Success message");

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/{sectionId}/lock", sectionId))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Success message"));
        }

        @Test
        @DisplayName("Exception Path: returns 403 when service throws RuntimeException")
        void lockTranscript_ServiceThrowsException_Returns403() throws Exception {
            // Arrange
            UUID sectionId = UUID.randomUUID();
            willThrow(new RuntimeException("Locking failed")).given(sectionService).lockSection(sectionId);

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/{sectionId}/lock", sectionId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Locking failed"));
        }
    }
}
