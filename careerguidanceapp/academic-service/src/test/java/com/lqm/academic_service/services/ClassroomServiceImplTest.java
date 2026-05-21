package com.lqm.academic_service.services;

import com.lqm.academic_service.events.ClassroomDeletedEvent;
import com.lqm.academic_service.events.ScoreSyncEvent;
import com.lqm.academic_service.events.StudentsRemovedEvent;
import com.lqm.academic_service.exceptions.BadRequestException;
import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.models.*;
import com.lqm.academic_service.repositories.ClassroomRepository;
import com.lqm.academic_service.repositories.SectionRepository;
import com.lqm.academic_service.repositories.StudentClassroomRepository;
import com.lqm.academic_service.services.Impl.ClassroomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for {@link ClassroomServiceImpl}.
 * Pattern: AAA (Arrange – Act – Assert).
 */
@ExtendWith(MockitoExtension.class)
class ClassroomServiceImplTest {

    // ─── Mocks ───────────────────────────────────────────────────────────────
    @Mock private ClassroomRepository classroomRepo;
    @Mock private MessageSource messageSource;
    @Mock private StudentClassroomRepository studentClassroomRepo;
    @Mock private GradeService gradeService;
    @Mock private SectionRepository sectionRepo;
    @Mock private CurriculumService curriculumService;
    @Mock private AcademicEventPublisher eventPublisher;

    @InjectMocks
    private ClassroomServiceImpl classroomService;

    // ─── Shared Fixtures ──────────────────────────────────────────────────────
    private UUID classroomId;
    private UUID gradeId;
    private UUID yearId;
    private Grade grade;
    private Classroom classroom;

    @BeforeEach
    void setUp() {
        classroomId = UUID.randomUUID();
        gradeId     = UUID.randomUUID();
        yearId      = UUID.randomUUID();

        org.mockito.Mockito.lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Year year = Year.builder().id(yearId).name("2024-2025").build();
        grade = Grade.builder().id(gradeId).name(GradeType.GRADE_10).year(year).build();
        classroom = Classroom.builder().id(classroomId).name("10A1").grade(grade).build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getClassroomById
    // ═══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getClassroomById")
    class GetClassroomByIdTests {

        @Test
        @DisplayName("Happy Path: returns classroom when found")
        void getClassroomById_WhenFound_ReturnsClassroom() {
            // Arrange
            given(classroomRepo.findById(classroomId)).willReturn(Optional.of(classroom));

            // Act
            Classroom result = classroomService.getClassroomById(classroomId);

            // Assert
            assertThat(result).isSameAs(classroom);
        }

        @Test
        @DisplayName("Exception Path: throws ResourceNotFoundException when not found")
        void getClassroomById_WhenNotFound_ThrowsResourceNotFoundException() {
            // Arrange
            given(classroomRepo.findById(classroomId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> classroomService.getClassroomById(classroomId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // saveClassroom
    // ═══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("saveClassroom")
    class SaveClassroomTests {

        @Test
        @DisplayName("Happy Path (new classroom): initializes sections from curriculum and publishes ScoreSyncEvent for added students")
        void saveClassroom_NewClassroomWithStudents_InitsSectionsAndPublishesScoreSync() {
            // Arrange
            UUID studentId = UUID.randomUUID();
            Classroom newClassroom = new Classroom(); // id == null → isNew = true

            // After save, the repo returns a persisted classroom with an id
            Classroom savedClassroom = Classroom.builder()
                    .id(classroomId)
                    .name("10A1")
                    .grade(grade)
                    .build();

            given(gradeService.getGradeById(gradeId)).willReturn(grade);
            given(classroomRepo.save(newClassroom)).willReturn(savedClassroom);

            // initSectionForClassroom: getCurriculums returns one curriculum
            Curriculum curriculum = Curriculum.builder().id(UUID.randomUUID()).build();
            given(curriculumService.getCurriculums(anyMap(), eq(Pageable.unpaged())))
                    .willReturn(new PageImpl<>(List.of(curriculum)));

            Section initSection = Section.builder()
                    .id(UUID.randomUUID())
                    .classroom(savedClassroom)
                    .curriculum(curriculum)
                    .scoreStatus(ScoreStatusType.DRAFT)
                    .build();
            given(sectionRepo.saveAll(anyList())).willReturn(List.of(initSection));

            // Act
            Classroom result = classroomService.saveClassroom(newClassroom, gradeId, List.of(studentId));

            // Assert
            assertThat(result).isSameAs(savedClassroom);

            ArgumentCaptor<ScoreSyncEvent> captor = ArgumentCaptor.forClass(ScoreSyncEvent.class);
            then(eventPublisher).should().publishScoreSync(captor.capture());
            ScoreSyncEvent event = captor.getValue();
            assertThat(event.newStudentIds()).containsExactly(studentId);
            assertThat(event.removedStudentIds()).isEmpty();
        }

        @Test
        @DisplayName("Happy Path (update classroom): detects removed students and publishes StudentsRemovedEvent")
        void saveClassroom_UpdateClassroomWithRemovedStudents_PublishesStudentsRemovedEvent() {
            // Arrange
            UUID existingStudentId = UUID.randomUUID(); // student that will be removed
            UUID newStudentId = UUID.randomUUID();       // student that stays

            // Simulate existing classroom with one student
            Classroom existingClassroom = Classroom.builder()
                    .id(classroomId)
                    .name("10A1")
                    .grade(grade)
                    .build();

            StudentClassroom sc = StudentClassroom.builder()
                    .studentId(existingStudentId)
                    .classroom(existingClassroom)
                    .build();
            existingClassroom.addStudent(sc);

            Section section = Section.builder().id(UUID.randomUUID()).build();
            // Manually add section to sectionSet via reflection is complex; instead use partial mock approach
            // We just need sections not empty for the event to be published.
            // Use a real Classroom with sections added.
            existingClassroom.getSectionSet().add(section);

            given(gradeService.getGradeById(gradeId)).willReturn(grade);
            given(classroomRepo.save(existingClassroom)).willReturn(existingClassroom);

            // Act: pass only newStudentId — so existingStudentId is "removed"
            classroomService.saveClassroom(existingClassroom, gradeId, List.of(newStudentId));

            // Assert: StudentsRemovedEvent published with the removed student
            ArgumentCaptor<StudentsRemovedEvent> removedCaptor = ArgumentCaptor.forClass(StudentsRemovedEvent.class);
            then(eventPublisher).should().publishStudentsRemoved(removedCaptor.capture());
            assertThat(removedCaptor.getValue().removedStudentIds()).containsExactly(existingStudentId);

            // ScoreSyncEvent also published
            ArgumentCaptor<ScoreSyncEvent> syncCaptor = ArgumentCaptor.forClass(ScoreSyncEvent.class);
            then(eventPublisher).should().publishScoreSync(syncCaptor.capture());
            assertThat(syncCaptor.getValue().removedStudentIds()).containsExactly(existingStudentId);
            assertThat(syncCaptor.getValue().newStudentIds()).containsExactly(newStudentId);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // deleteClassroom
    // ═══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("deleteClassroom")
    class DeleteClassroomTests {

        @Test
        @DisplayName("Exception Path: throws BadRequestException when classroom still has students")
        void deleteClassroom_WhenHasStudents_ThrowsBadRequestException() {
            // Arrange
            given(studentClassroomRepo.existsByClassroomId(classroomId)).willReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> classroomService.deleteClassroom(classroomId))
                    .isInstanceOf(BadRequestException.class);

            then(classroomRepo).should(never()).deleteById(any());
            then(eventPublisher).should(never()).publishClassroomDeleted(any());
        }

        @Test
        @DisplayName("Happy Path: deletes classroom and publishes ClassroomDeletedEvent + ScoreSyncEvent + ChatGroupDeleteEvent")
        void deleteClassroom_WhenNoStudents_DeletesAndPublishesAllEvents() {
            // Arrange
            UUID sectionId = UUID.randomUUID();
            Section section = Section.builder().id(sectionId).build();
            classroom.getSectionSet().add(section);

            given(studentClassroomRepo.existsByClassroomId(classroomId)).willReturn(false);
            given(classroomRepo.findById(classroomId)).willReturn(Optional.of(classroom));

            // Act
            classroomService.deleteClassroom(classroomId);

            // Assert
            then(classroomRepo).should().deleteById(classroomId);

            ArgumentCaptor<ClassroomDeletedEvent> classroomDeletedCaptor =
                    ArgumentCaptor.forClass(ClassroomDeletedEvent.class);
            then(eventPublisher).should().publishClassroomDeleted(classroomDeletedCaptor.capture());
            assertThat(classroomDeletedCaptor.getValue().classroomId()).isEqualTo(classroomId);
            assertThat(classroomDeletedCaptor.getValue().sectionIds()).containsExactly(sectionId);

            then(eventPublisher).should().publishScoreSync(any(ScoreSyncEvent.class));
            then(eventPublisher).should().publishChatGroupDelete(any());
        }

        @Test
        @DisplayName("Happy Path: no section events published when classroom has no sections")
        void deleteClassroom_WithNoSections_PublishesOnlyClassroomDeletedEvent() {
            // Arrange
            given(studentClassroomRepo.existsByClassroomId(classroomId)).willReturn(false);
            // classroom with no sections
            Classroom emptyClassroom = Classroom.builder().id(classroomId).name("10A1").grade(grade).build();
            given(classroomRepo.findById(classroomId)).willReturn(Optional.of(emptyClassroom));

            // Act
            classroomService.deleteClassroom(classroomId);

            // Assert
            then(classroomRepo).should().deleteById(classroomId);
            then(eventPublisher).should().publishClassroomDeleted(any(ClassroomDeletedEvent.class));
            then(eventPublisher).should(never()).publishScoreSync(any());
            then(eventPublisher).should(never()).publishChatGroupDelete(any());
        }
    }
}
