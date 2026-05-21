package com.lqm.chat_service.it;

import com.lqm.chat_service.BaseIntegrationTest;
import com.lqm.chat_service.config.RabbitMQConfig;
import com.lqm.chat_service.event.ChatGroupCreateEvent;
import com.lqm.chat_service.event.ChatGroupDeleteEvent;
import com.lqm.chat_service.event.ChatGroupUpdateTeacherEvent;
import com.lqm.chat_service.service.FirestoreGroupChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Integration Test cho ChatEventConsumer.
 * Kiểm tra toàn bộ luồng: Publish message vào RabbitMQ → Consumer nhận → gọi
 * FirestoreGroupChatService.
 */
@DisplayName("ChatEventConsumerIT — RabbitMQ Consumer Integration Tests")
class ChatEventConsumerIT extends BaseIntegrationTest {

        @Autowired
        private RabbitTemplate rabbitTemplate;

        @Autowired
        private FirestoreGroupChatService firestoreGroupChatService;

        private static final long CONSUMER_TIMEOUT_MS = 5000;

        @BeforeEach
        void resetMocks() {
                Mockito.reset(firestoreGroupChatService);
        }

        @Test
        @DisplayName("Publish ChatGroupCreateEvent → Consumer gọi createGroupChat()")
        void handleGroupCreate_ShouldCallCreateGroupChat() {
                // Given
                UUID sectionId = UUID.randomUUID();
                List<String> students = List.of("student1@school.edu", "student2@school.edu");
                ChatGroupCreateEvent event = new ChatGroupCreateEvent(sectionId, "Toán 10A", "teacher@school.edu",
                                students);

                // When
                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE,
                                RabbitMQConfig.RK_GROUP_CREATE,
                                event);

                // Then — đợi consumer xử lý bất đồng bộ
                verify(firestoreGroupChatService, timeout(CONSUMER_TIMEOUT_MS))
                                .createGroupChat(eq(sectionId), eq("Toán 10A"), eq("teacher@school.edu"), eq(students));
        }

        @Test
        @DisplayName("Publish ChatGroupUpdateTeacherEvent → Consumer gọi updateTeacher()")
        void handleUpdateTeacher_ShouldCallUpdateTeacher() {
                // Given
                UUID sectionId = UUID.randomUUID();
                ChatGroupUpdateTeacherEvent event = new ChatGroupUpdateTeacherEvent(
                                sectionId, "old.teacher@school.edu", "new.teacher@school.edu");

                // When
                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE,
                                RabbitMQConfig.RK_GROUP_UPDATE_TEACHER,
                                event);

                // Then
                verify(firestoreGroupChatService, timeout(CONSUMER_TIMEOUT_MS))
                                .updateTeacher(
                                                eq(sectionId),
                                                eq("old.teacher@school.edu"),
                                                eq("new.teacher@school.edu"));
        }

        @Test
        @DisplayName("Publish ChatGroupDeleteEvent (1 section) → Consumer gọi deleteGroupChat()")
        void handleGroupDelete_SingleSection_ShouldCallDeleteGroupChat() {
                // Given
                UUID sectionId = UUID.randomUUID();
                ChatGroupDeleteEvent event = new ChatGroupDeleteEvent(List.of(sectionId));

                // When
                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE,
                                RabbitMQConfig.RK_GROUP_DELETE,
                                event);

                // Then
                verify(firestoreGroupChatService, timeout(CONSUMER_TIMEOUT_MS))
                                .deleteGroupChat(eq(sectionId));
        }

        @Test
        @DisplayName("Publish ChatGroupDeleteEvent (nhiều section) → Consumer gọi deleteGroupChatsBatch()")
        void handleGroupDelete_MultipleSections_ShouldCallDeleteGroupChatsBatch() {
                // Given
                List<UUID> sectionIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
                ChatGroupDeleteEvent event = new ChatGroupDeleteEvent(sectionIds);

                // When
                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE,
                                RabbitMQConfig.RK_GROUP_DELETE,
                                event);

                // Then
                verify(firestoreGroupChatService, timeout(CONSUMER_TIMEOUT_MS))
                                .deleteGroupChatsBatch(eq(sectionIds));
        }
}
