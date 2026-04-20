package com.lqm.chat_service.consumer;

import com.lqm.chat_service.config.RabbitMQConfig;
import com.lqm.chat_service.event.ChatGroupCreateEvent;
import com.lqm.chat_service.event.ChatGroupDeleteEvent;
import com.lqm.chat_service.event.ChatGroupUpdateTeacherEvent;
import com.lqm.chat_service.service.FirestoreGroupChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatEventConsumer {

    private final FirestoreGroupChatService groupChatService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_GROUP_CREATE)
    public void handleGroupCreate(ChatGroupCreateEvent event) {
        log.info("Received ChatGroupCreateEvent: sectionId={}", event.sectionId());
        try {
            groupChatService.createGroupChat(
                    event.sectionId(),
                    event.groupName(),
                    event.teacherEmail(),
                    event.studentEmails());
            log.info("Successfully created group chat for section {}", event.sectionId());
        } catch (Exception e) {
            log.error("Error creating group chat for section {}: {}", event.sectionId(), e.getMessage(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_GROUP_UPDATE_TEACHER)
    public void handleUpdateTeacher(ChatGroupUpdateTeacherEvent event) {
        log.info("Received ChatGroupUpdateTeacherEvent: sectionId={}", event.sectionId());
        try {
            groupChatService.updateTeacher(
                    event.sectionId(),
                    event.oldTeacherEmail(),
                    event.newTeacherEmail());
            log.info("Successfully updated teacher for section {}", event.sectionId());
        } catch (Exception e) {
            log.error("Error updating teacher for section {}: {}", event.sectionId(), e.getMessage(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_GROUP_DELETE)
    public void handleGroupDelete(ChatGroupDeleteEvent event) {
        log.info("Received ChatGroupDeleteEvent: sectionIds={}", event.sectionIds());
        try {
            if (event.sectionIds().size() == 1) {
                groupChatService.deleteGroupChat(event.sectionIds().getFirst());
            } else {
                groupChatService.deleteGroupChatsBatch(event.sectionIds());
            }
            log.info("Successfully deleted {} group chat(s)", event.sectionIds().size());
        } catch (Exception e) {
            log.error("Error deleting group chats: {}", e.getMessage(), e);
            throw e;
        }
    }
}
