package com.lqm.chat_service.consumer;

import com.lqm.chat_service.event.ChatGroupCreateEvent;
import com.lqm.chat_service.event.ChatGroupDeleteEvent;
import com.lqm.chat_service.event.ChatGroupUpdateTeacherEvent;
import com.lqm.chat_service.service.FirestoreGroupChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatEventConsumerTest {

    @Mock
    private FirestoreGroupChatService groupChatService;

    @InjectMocks
    private ChatEventConsumer chatEventConsumer;

    @Test
    @DisplayName("handleGroupCreate: success")
    void handleGroupCreate_Success() {
        UUID sectionId = UUID.randomUUID();
        ChatGroupCreateEvent event = new ChatGroupCreateEvent(sectionId, "10A1", "teacher@test.com", List.of("student@test.com"));

        doNothing().when(groupChatService).createGroupChat(any(), any(), any(), any());

        chatEventConsumer.handleGroupCreate(event);

        verify(groupChatService).createGroupChat(sectionId, "10A1", "teacher@test.com", List.of("student@test.com"));
    }

    @Test
    @DisplayName("handleGroupCreate: throws exception")
    void handleGroupCreate_ThrowsException() {
        UUID sectionId = UUID.randomUUID();
        ChatGroupCreateEvent event = new ChatGroupCreateEvent(sectionId, "10A1", "teacher@test.com", List.of("student@test.com"));

        doThrow(new RuntimeException("DB Error")).when(groupChatService).createGroupChat(any(), any(), any(), any());

        assertThatThrownBy(() -> chatEventConsumer.handleGroupCreate(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB Error");
    }

    @Test
    @DisplayName("handleUpdateTeacher: success")
    void handleUpdateTeacher_Success() {
        UUID sectionId = UUID.randomUUID();
        ChatGroupUpdateTeacherEvent event = new ChatGroupUpdateTeacherEvent(sectionId, "old@test.com", "new@test.com");

        doNothing().when(groupChatService).updateTeacher(any(), any(), any());

        chatEventConsumer.handleUpdateTeacher(event);

        verify(groupChatService).updateTeacher(sectionId, "old@test.com", "new@test.com");
    }

    @Test
    @DisplayName("handleUpdateTeacher: throws exception")
    void handleUpdateTeacher_ThrowsException() {
        UUID sectionId = UUID.randomUUID();
        ChatGroupUpdateTeacherEvent event = new ChatGroupUpdateTeacherEvent(sectionId, "old@test.com", "new@test.com");

        doThrow(new RuntimeException("DB Error")).when(groupChatService).updateTeacher(any(), any(), any());

        assertThatThrownBy(() -> chatEventConsumer.handleUpdateTeacher(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB Error");
    }

    @Test
    @DisplayName("handleGroupDelete: single section ID")
    void handleGroupDelete_Single() {
        UUID sectionId = UUID.randomUUID();
        ChatGroupDeleteEvent event = new ChatGroupDeleteEvent(List.of(sectionId));

        doNothing().when(groupChatService).deleteGroupChat(sectionId);

        chatEventConsumer.handleGroupDelete(event);

        verify(groupChatService).deleteGroupChat(sectionId);
        verify(groupChatService, never()).deleteGroupChatsBatch(any());
    }

    @Test
    @DisplayName("handleGroupDelete: multiple section IDs")
    void handleGroupDelete_Multiple() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        ChatGroupDeleteEvent event = new ChatGroupDeleteEvent(List.of(id1, id2));

        doNothing().when(groupChatService).deleteGroupChatsBatch(List.of(id1, id2));

        chatEventConsumer.handleGroupDelete(event);

        verify(groupChatService).deleteGroupChatsBatch(List.of(id1, id2));
        verify(groupChatService, never()).deleteGroupChat(any());
    }

    @Test
    @DisplayName("handleGroupDelete: throws exception")
    void handleGroupDelete_ThrowsException() {
        UUID sectionId = UUID.randomUUID();
        ChatGroupDeleteEvent event = new ChatGroupDeleteEvent(List.of(sectionId));

        doThrow(new RuntimeException("DB Error")).when(groupChatService).deleteGroupChat(sectionId);

        assertThatThrownBy(() -> chatEventConsumer.handleGroupDelete(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB Error");
    }
}
