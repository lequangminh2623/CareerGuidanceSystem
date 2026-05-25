package com.lqm.academic_service.consumers;

import com.lqm.academic_service.events.UserDeletedEvent;
import com.lqm.academic_service.models.Classroom;
import com.lqm.academic_service.models.Grade;
import com.lqm.academic_service.models.Section;
import com.lqm.academic_service.models.StudentClassroom;
import com.lqm.academic_service.models.Curriculum;
import com.lqm.academic_service.services.ClassroomService;
import com.lqm.academic_service.services.SectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserEventConsumer Unit Tests")
class UserEventConsumerTest {

    @Mock
    private ClassroomService classroomService;

    @Mock
    private SectionService sectionService;

    @InjectMocks
    private UserEventConsumer userEventConsumer;

    @Test
    @DisplayName("Should process ROLE_STUDENT and remove student from classroom")
    void testConsumeUserDeletedEvent_StudentRole() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        UserDeletedEvent event = new UserDeletedEvent(studentId, "ROLE_STUDENT");

        UUID classroomId = UUID.randomUUID();
        UUID gradeId = UUID.randomUUID();
        UUID anotherStudentId = UUID.randomUUID();

        Grade grade = new Grade();
        grade.setId(gradeId);

        Classroom classroom = new Classroom();
        classroom.setId(classroomId);
        classroom.setGrade(grade);

        StudentClassroom sc1 = StudentClassroom.builder().studentId(studentId).build();
        StudentClassroom sc2 = StudentClassroom.builder().studentId(anotherStudentId).build();
        classroom.addStudent(sc1);
        classroom.addStudent(sc2);

        when(classroomService.getClassrooms(
                eq(Map.of("studentId", studentId.toString())),
                any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(classroom)));

        // Act
        userEventConsumer.consumeUserDeletedEvent(event);

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UUID>> studentIdsCaptor = ArgumentCaptor.forClass(List.class);

        verify(classroomService).saveClassroom(eq(classroom), eq(gradeId), studentIdsCaptor.capture());

        List<UUID> capturedIds = studentIdsCaptor.getValue();
        assertThat(capturedIds).containsExactly(anotherStudentId);
        assertThat(capturedIds).doesNotContain(studentId);
    }

    @Test
    @DisplayName("Should process ROLE_TEACHER and set section teacherId to null")
    void testConsumeUserDeletedEvent_TeacherRole() {
        // Arrange
        UUID teacherId = UUID.randomUUID();
        UserDeletedEvent event = new UserDeletedEvent(teacherId, "ROLE_TEACHER");

        UUID sectionId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();

        Classroom classroom = new Classroom();
        classroom.setId(classroomId);

        Curriculum curriculum = new Curriculum();
        curriculum.setId(curriculumId);

        Section section = new Section();
        section.setId(sectionId);
        section.setTeacherId(teacherId);
        section.setClassroom(classroom);
        section.setCurriculum(curriculum);

        when(sectionService.getSections(
                eq(Map.of("teacherId", teacherId.toString())),
                any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(section)));

        // Act
        userEventConsumer.consumeUserDeletedEvent(event);

        // Assert
        verify(sectionService).saveSingleSection(eq(section), eq(classroomId), eq(curriculumId));
        assertThat(section.getTeacherId()).isNull();
    }
}
