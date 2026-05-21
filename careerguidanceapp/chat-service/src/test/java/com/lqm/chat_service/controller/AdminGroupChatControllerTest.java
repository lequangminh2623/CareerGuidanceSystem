package com.lqm.chat_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.chat_service.service.FirestoreGroupChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminGroupChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminGroupChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FirestoreGroupChatService groupChatService;

    private static final String BASE_URL = "/api/internal/admin/chats/groups";

    @Nested
    @DisplayName("POST /")
    class CreateGroupTests {
        @Test
        @DisplayName("Happy Path: returns 201")
        void createGroup_Returns201() throws Exception {
            UUID sectionId = UUID.randomUUID();
            AdminGroupChatController.CreateGroupRequest request = new AdminGroupChatController.CreateGroupRequest(
                    sectionId, "10A1", "teacher@test.com", List.of("student1@test.com"));

            willDoNothing().given(groupChatService).createGroupChat(any(), any(), any(), any());

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("PUT /teacher")
    class UpdateTeacherTests {
        @Test
        @DisplayName("Happy Path: returns 204")
        void updateTeacher_Returns204() throws Exception {
            UUID sectionId = UUID.randomUUID();
            AdminGroupChatController.UpdateTeacherRequest request = new AdminGroupChatController.UpdateTeacherRequest(
                    sectionId, "teacher@test.com", "new_teacher@test.com");

            willDoNothing().given(groupChatService).updateTeacher(any(), any(), any());

            mockMvc.perform(put(BASE_URL + "/teacher")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("DELETE /{sectionId}")
    class DeleteGroupTests {
        @Test
        @DisplayName("Happy Path: returns 204")
        void deleteGroup_Returns204() throws Exception {
            UUID sectionId = UUID.randomUUID();
            willDoNothing().given(groupChatService).deleteGroupChat(sectionId);

            mockMvc.perform(delete(BASE_URL + "/{sectionId}", sectionId))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("DELETE /batch")
    class DeleteGroupsBatchTests {
        @Test
        @DisplayName("Happy Path: returns 204")
        void deleteGroupsBatch_Returns204() throws Exception {
            UUID sectionId1 = UUID.randomUUID();
            UUID sectionId2 = UUID.randomUUID();
            List<UUID> ids = List.of(sectionId1, sectionId2);
            willDoNothing().given(groupChatService).deleteGroupChatsBatch(ids);

            mockMvc.perform(delete(BASE_URL + "/batch")
                    .param("sectionIds", sectionId1.toString(), sectionId2.toString()))
                    .andExpect(status().isNoContent());
        }
    }
}
