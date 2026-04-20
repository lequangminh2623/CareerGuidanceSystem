package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.configs.RabbitMQConfig;
import com.lqm.academic_service.events.*;
import com.lqm.academic_service.services.AcademicEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcademicEventPublisherImpl implements AcademicEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishScoreSync(ScoreSyncEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE, RabbitMQConfig.RK_SCORE_SYNC, event);
        log.info("Published ScoreSyncEvent: sectionIds={}, newStudents={}, removedStudents={}",
                event.sectionIds().size(), event.newStudentIds().size(), event.removedStudentIds().size());
    }

    @Override
    public void publishStudentsRemoved(StudentsRemovedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE, RabbitMQConfig.RK_STUDENTS_REMOVED, event);
        log.info("Published StudentsRemovedEvent: classroomId={}, removedStudents={}",
                event.classroomId(), event.removedStudentIds().size());
    }

    @Override
    public void publishClassroomDeleted(ClassroomDeletedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE, RabbitMQConfig.RK_CLASSROOM_DELETED, event);
        log.info("Published ClassroomDeletedEvent: classroomId={}, sections={}", event.classroomId(), event.sectionIds().size());
    }

    @Override
    public void publishChatGroupCreate(ChatGroupCreateEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE, RabbitMQConfig.RK_CHAT_GROUP_CREATE, event);
        log.info("Published ChatGroupCreateEvent: sectionId={}", event.sectionId());
    }

    @Override
    public void publishChatGroupUpdateTeacher(ChatGroupUpdateTeacherEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE, RabbitMQConfig.RK_CHAT_GROUP_UPDATE_TEACHER, event);
        log.info("Published ChatGroupUpdateTeacherEvent: sectionId={}", event.sectionId());
    }

    @Override
    public void publishChatGroupDelete(ChatGroupDeleteEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE, RabbitMQConfig.RK_CHAT_GROUP_DELETE, event);
        log.info("Published ChatGroupDeleteEvent: sectionIds={}", event.sectionIds());
    }
}
