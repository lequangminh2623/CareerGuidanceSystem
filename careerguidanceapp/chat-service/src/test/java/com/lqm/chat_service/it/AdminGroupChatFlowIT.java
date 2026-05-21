package com.lqm.chat_service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.chat_service.BaseIntegrationTest;
import com.lqm.chat_service.service.FirestoreGroupChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Test cho AdminGroupChatController.
 * Base path: /api/internal/admin/chats/groups (public — permitAll trong
 * SecurityConfig)
 */
@DisplayName("AdminGroupChatFlowIT — Internal Admin Group Chat REST Tests")
class AdminGroupChatFlowIT extends BaseIntegrationTest {

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private FirestoreGroupChatService firestoreGroupChatService;

        @BeforeEach
        void resetMocks() {
                Mockito.reset(firestoreGroupChatService);
        }

        @Test
        @DisplayName("POST /api/internal/admin/chats/groups → 201, gọi createGroupChat()")
        void createGroup_ShouldReturn201() throws Exception {
                // Given
                UUID sectionId = UUID.randomUUID();
                Map<String, Object> body = Map.of(
                                "sectionId", sectionId.toString(),
                                "groupName", "Toán 10A1",
                                "teacherEmail", "teacher@school.edu",
                                "studentEmails", List.of("s1@school.edu", "s2@school.edu"));

                // When & Then
                mockMvc.perform(post("/api/internal/admin/chats/groups")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                                .andExpect(status().isCreated());

                verify(firestoreGroupChatService).createGroupChat(
                                any(UUID.class), any(), any(), anyList());
        }

        @Test
        @DisplayName("PUT /api/internal/admin/chats/groups/teacher → 204, gọi updateTeacher()")
        void updateTeacher_ShouldReturn204() throws Exception {
                // Given
                UUID sectionId = UUID.randomUUID();
                Map<String, Object> body = Map.of(
                                "sectionId", sectionId.toString(),
                                "oldTeacherEmail", "old@school.edu",
                                "newTeacherEmail", "new@school.edu");

                // When & Then
                mockMvc.perform(put("/api/internal/admin/chats/groups/teacher")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                                .andExpect(status().isNoContent());

                verify(firestoreGroupChatService).updateTeacher(any(UUID.class), any(), any());
        }

        @Test
        @DisplayName("DELETE /api/internal/admin/chats/groups/{sectionId} → 204, gọi deleteGroupChat()")
        void deleteGroup_ShouldReturn204() throws Exception {
                // Given
                UUID sectionId = UUID.randomUUID();

                // When & Then
                mockMvc.perform(delete("/api/internal/admin/chats/groups/" + sectionId))
                                .andExpect(status().isNoContent());

                verify(firestoreGroupChatService).deleteGroupChat(sectionId);
        }

        @Test
        @DisplayName("DELETE /api/internal/admin/chats/groups/batch → 204, gọi deleteGroupChatsBatch()")
        void deleteGroupsBatch_ShouldReturn204() throws Exception {
                // Given
                UUID id1 = UUID.randomUUID();
                UUID id2 = UUID.randomUUID();

                // When & Then
                mockMvc.perform(delete("/api/internal/admin/chats/groups/batch")
                                .param("sectionIds", id1.toString(), id2.toString()))
                                .andExpect(status().isNoContent());

                verify(firestoreGroupChatService).deleteGroupChatsBatch(anyList());
        }
}
