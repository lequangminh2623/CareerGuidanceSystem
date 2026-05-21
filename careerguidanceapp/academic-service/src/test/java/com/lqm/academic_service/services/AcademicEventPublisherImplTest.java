package com.lqm.academic_service.services;

import com.lqm.academic_service.configs.RabbitMQConfig;
import com.lqm.academic_service.events.*;
import com.lqm.academic_service.services.Impl.AcademicEventPublisherImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.eq;

/**
 * Unit tests for {@link AcademicEventPublisherImpl}.
 * Verifies that each publish method calls RabbitTemplate with the correct
 * exchange, routing key, and payload — without any Spring context.
 */
@ExtendWith(MockitoExtension.class)
class AcademicEventPublisherImplTest {

    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AcademicEventPublisherImpl eventPublisher;

    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("publishScoreSync — sends to correct exchange and routing key")
    void publishScoreSync_SendsToCorrectExchangeAndRoutingKey() {
        // Arrange
        ScoreSyncEvent event = new ScoreSyncEvent(
                List.of(UUID.randomUUID()),
                List.of(UUID.randomUUID()),
                Collections.emptyList());

        // Act
        eventPublisher.publishScoreSync(event);

        // Assert
        then(rabbitTemplate).should().convertAndSend(
                eq(RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE),
                eq(RabbitMQConfig.RK_SCORE_SYNC),
                eq(event));
    }

    @Test
    @DisplayName("publishStudentsRemoved — sends to correct exchange and routing key")
    void publishStudentsRemoved_SendsToCorrectExchangeAndRoutingKey() {
        // Arrange
        StudentsRemovedEvent event = new StudentsRemovedEvent(UUID.randomUUID(), List.of(UUID.randomUUID()));

        // Act
        eventPublisher.publishStudentsRemoved(event);

        // Assert
        then(rabbitTemplate).should().convertAndSend(
                eq(RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE),
                eq(RabbitMQConfig.RK_STUDENTS_REMOVED),
                eq(event));
    }

    @Test
    @DisplayName("publishClassroomDeleted — sends to correct exchange and routing key")
    void publishClassroomDeleted_SendsToCorrectExchangeAndRoutingKey() {
        // Arrange
        ClassroomDeletedEvent event = new ClassroomDeletedEvent(UUID.randomUUID(), List.of(UUID.randomUUID()));

        // Act
        eventPublisher.publishClassroomDeleted(event);

        // Assert
        then(rabbitTemplate).should().convertAndSend(
                eq(RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE),
                eq(RabbitMQConfig.RK_CLASSROOM_DELETED),
                eq(event));
    }

    @Test
    @DisplayName("publishChatGroupCreate — sends correct payload with all fields")
    void publishChatGroupCreate_SendsCorrectPayload() {
        // Arrange
        UUID sectionId = UUID.randomUUID();
        ChatGroupCreateEvent event = new ChatGroupCreateEvent(
                sectionId, "10A1 - Toán", "teacher@example.com", List.of("s1@example.com"));

        // Act
        eventPublisher.publishChatGroupCreate(event);

        // Assert — capture payload and verify fields
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        then(rabbitTemplate).should().convertAndSend(
                eq(RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE),
                eq(RabbitMQConfig.RK_CHAT_GROUP_CREATE),
                payloadCaptor.capture());

        ChatGroupCreateEvent captured = (ChatGroupCreateEvent) payloadCaptor.getValue();
        assertThat(captured.sectionId()).isEqualTo(sectionId);
        assertThat(captured.teacherEmail()).isEqualTo("teacher@example.com");
        assertThat(captured.groupName()).isEqualTo("10A1 - Toán");
    }

    @Test
    @DisplayName("publishChatGroupUpdateTeacher — sends to correct routing key")
    void publishChatGroupUpdateTeacher_SendsToCorrectRoutingKey() {
        // Arrange
        ChatGroupUpdateTeacherEvent event = new ChatGroupUpdateTeacherEvent(
                UUID.randomUUID(), "old@example.com", "new@example.com");

        // Act
        eventPublisher.publishChatGroupUpdateTeacher(event);

        // Assert
        then(rabbitTemplate).should().convertAndSend(
                eq(RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE),
                eq(RabbitMQConfig.RK_CHAT_GROUP_UPDATE_TEACHER),
                eq(event));
    }

    @Test
    @DisplayName("publishChatGroupDelete — sends to correct routing key")
    void publishChatGroupDelete_SendsToCorrectRoutingKey() {
        // Arrange
        ChatGroupDeleteEvent event = new ChatGroupDeleteEvent(List.of(UUID.randomUUID()));

        // Act
        eventPublisher.publishChatGroupDelete(event);

        // Assert
        then(rabbitTemplate).should().convertAndSend(
                eq(RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE),
                eq(RabbitMQConfig.RK_CHAT_GROUP_DELETE),
                eq(event));
    }
}
