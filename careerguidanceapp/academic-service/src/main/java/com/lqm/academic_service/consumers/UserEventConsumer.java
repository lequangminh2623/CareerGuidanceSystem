package com.lqm.academic_service.consumers;

import com.lqm.academic_service.configs.RabbitMQConfig;
import com.lqm.academic_service.events.UserDeletedEvent;
import com.lqm.academic_service.models.Classroom;
import com.lqm.academic_service.models.Section;
import com.lqm.academic_service.models.StudentClassroom;
import com.lqm.academic_service.services.ClassroomService;
import com.lqm.academic_service.services.SectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final ClassroomService classroomService;
    private final SectionService sectionService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ACADEMIC_USER_DELETED)
    @Transactional
    public void consumeUserDeletedEvent(UserDeletedEvent event) {
        log.info("Received UserDeletedEvent: userId={}, role={}", event.userId(), event.role());

        try {
            if ("ROLE_STUDENT".equals(event.role())) {
                Page<Classroom> classrooms = classroomService.getClassrooms(
                        Map.of("studentId", event.userId().toString()),
                        PageRequest.of(0, 100));

                for (Classroom classroom : classrooms.getContent()) {
                    // Collect all existing student IDs except the deleted one
                    List<UUID> newStudentIds = classroom.getStudentClassroomSet().stream()
                            .map(StudentClassroom::getStudentId)
                            .filter(id -> !id.equals(event.userId()))
                            .collect(Collectors.toList());

                    classroomService.saveClassroom(classroom, classroom.getGrade().getId(), newStudentIds);
                }
            } else if ("ROLE_TEACHER".equals(event.role())) {
                Page<Section> sections = sectionService.getSections(
                        Map.of("teacherId", event.userId().toString()),
                        PageRequest.of(0, 100));

                for (Section section : sections.getContent()) {
                    sectionService.removeTeacherFromSection(section.getId());
                }
            }
        } catch (Exception e) {
            log.error("Error processing UserDeletedEvent for userId={}", event.userId(), e);
            throw e;
        }
    }
}
