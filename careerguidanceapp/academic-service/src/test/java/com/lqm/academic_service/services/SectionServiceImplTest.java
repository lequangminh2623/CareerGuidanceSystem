package com.lqm.academic_service.services;

import com.lqm.academic_service.clients.ScoreClient;
import com.lqm.academic_service.clients.UserClient;
import com.lqm.academic_service.dtos.UserMessageResponseDTO;
import com.lqm.academic_service.events.ChatGroupCreateEvent;
import com.lqm.academic_service.events.ChatGroupDeleteEvent;
import com.lqm.academic_service.events.ScoreSyncEvent;
import com.lqm.academic_service.exceptions.BadRequestException;
import com.lqm.academic_service.exceptions.ForbiddenException;
import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.models.*;
import com.lqm.academic_service.repositories.SectionRepository;
import com.lqm.academic_service.services.Impl.SectionServiceImpl;
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

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for {@link SectionServiceImpl}.
 * Uses @ExtendWith(MockitoExtension.class) — NO Spring context started.
 * Pattern: AAA (Arrange – Act – Assert).
 */
@ExtendWith(MockitoExtension.class)
class SectionServiceImplTest {

        // ─── Mocks ───────────────────────────────────────────────────────────────
        @Mock
        private SectionRepository sectionRepo;
        @Mock
        private MessageSource messageSource;
        @Mock
        private ClassroomService classroomService;
        @Mock
        private CurriculumService curriculumService;
        @Mock
        private UserClient userClient;
        @Mock
        private ScoreClient scoreClient;
        @Mock
        private AcademicEventPublisher eventPublisher;
        @Mock
        private EmailPublisher emailPublisher;

        @InjectMocks
        private SectionServiceImpl sectionService;

        // ─── Shared Fixtures ──────────────────────────────────────────────────────
        private UUID sectionId;
        private UUID classroomId;
        private UUID curriculumId;
        private Section draftSection;
        private Section lockedSection;
        private Classroom classroom;
        private Curriculum curriculum;

        @BeforeEach
        void setUp() {
                sectionId = UUID.randomUUID();
                classroomId = UUID.randomUUID();
                curriculumId = UUID.randomUUID();

                // Stub default message source to echo the key so assertions are deterministic
                org.mockito.Mockito.lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                Subject subject = new Subject();
                subject.setName("Toán");

                Year year = Year.builder().name("2024-2025").build();
                Semester semester = Semester.builder()
                                .name(SemesterType.SEMESTER_1)
                                .year(year)
                                .build();

                curriculum = Curriculum.builder()
                                .id(curriculumId)
                                .subject(subject)
                                .semester(semester)
                                .build();

                Grade grade = Grade.builder()
                                .name(GradeType.GRADE_10)
                                .year(year)
                                .build();

                classroom = Classroom.builder()
                                .id(classroomId)
                                .name("10A1")
                                .grade(grade)
                                .build();

                draftSection = Section.builder()
                                .id(sectionId)
                                .scoreStatus(ScoreStatusType.DRAFT)
                                .classroom(classroom)
                                .curriculum(curriculum)
                                .build();

                lockedSection = Section.builder()
                                .id(sectionId)
                                .scoreStatus(ScoreStatusType.LOCKED)
                                .classroom(classroom)
                                .curriculum(curriculum)
                                .build();
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // getSectionById
        // ═══════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("getSectionById")
        class GetSectionByIdTests {

                @Test
                @DisplayName("Happy Path: returns section when it exists")
                void getSectionById_WhenFound_ReturnsSection() {
                        // Arrange
                        given(sectionRepo.findById(sectionId)).willReturn(Optional.of(draftSection));

                        // Act
                        Section result = sectionService.getSectionById(sectionId);

                        // Assert
                        assertThat(result).isSameAs(draftSection);
                }

                @Test
                @DisplayName("Exception Path: throws ResourceNotFoundException when section not found")
                void getSectionById_WhenNotFound_ThrowsResourceNotFoundException() {
                        // Arrange
                        given(sectionRepo.findById(sectionId)).willReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> sectionService.getSectionById(sectionId))
                                        .isInstanceOf(ResourceNotFoundException.class);
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // lockSection
        // ═══════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("lockSection")
        class LockSectionTests {

                @Test
                @DisplayName("Happy Path: locks section, saves it, and sends email notifications")
                void lockSection_WhenDraftAndFullyScored_LocksAndNotifiesStudents() {
                        // Arrange
                        UUID studentId = UUID.randomUUID();
                        StudentClassroom sc = StudentClassroom.builder()
                                        .studentId(studentId)
                                        .classroom(classroom)
                                        .build();
                        classroom.addStudent(sc);

                        given(sectionRepo.findById(sectionId)).willReturn(Optional.of(draftSection));
                        given(scoreClient.isTranscriptFullyScored(sectionId)).willReturn(Boolean.TRUE);

                        UserMessageResponseDTO studentMsg = new UserMessageResponseDTO(
                                        "An", "Nguyen", "an@example.com", null, "Student");
                        given(userClient.getUsersMessages(eq(List.of(studentId)), anyMap()))
                                        .willReturn(new PageImpl<>(List.of(studentMsg)));
                        given(sectionRepo.save(any(Section.class))).willAnswer(inv -> inv.getArgument(0));

                        // Act
                        sectionService.lockSection(sectionId);

                        // Assert — status changed
                        assertThat(draftSection.getScoreStatus()).isEqualTo(ScoreStatusType.LOCKED);
                        then(sectionRepo).should().save(draftSection);
                        // Email was sent for the student
                        then(emailPublisher).should(atLeastOnce()).publish(any());
                }

                @Test
                @DisplayName("Exception Path: throws BadRequestException when section is already locked")
                void lockSection_WhenAlreadyLocked_ThrowsBadRequestException() {
                        // Arrange
                        given(sectionRepo.findById(sectionId)).willReturn(Optional.of(lockedSection));

                        // Act & Assert
                        assertThatThrownBy(() -> sectionService.lockSection(sectionId))
                                        .isInstanceOf(BadRequestException.class);

                        then(scoreClient).should(never()).isTranscriptFullyScored(any());
                        then(sectionRepo).should(never()).save(any());
                }

                @Test
                @DisplayName("Exception Path: throws ForbiddenException when transcript is not fully scored")
                void lockSection_WhenNotFullyScored_ThrowsForbiddenException() {
                        // Arrange
                        given(sectionRepo.findById(sectionId)).willReturn(Optional.of(draftSection));
                        given(scoreClient.isTranscriptFullyScored(sectionId)).willReturn(Boolean.FALSE);

                        // Act & Assert
                        assertThatThrownBy(() -> sectionService.lockSection(sectionId))
                                        .isInstanceOf(ForbiddenException.class);

                        then(sectionRepo).should(never()).save(any());
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // saveSingleSection
        // ═══════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("saveSingleSection")
        class SaveSingleSectionTests {

                @Test
                @DisplayName("Happy Path: saves section and publishes ScoreSyncEvent when classroom has students")
                void saveSingleSection_WithStudents_PublishesScoreSyncEvent() {
                        // Arrange
                        UUID studentId = UUID.randomUUID();
                        StudentClassroom sc = StudentClassroom.builder()
                                        .studentId(studentId)
                                        .classroom(classroom)
                                        .build();
                        classroom.addStudent(sc);

                        Section inputSection = Section.builder()
                                        .scoreStatus(ScoreStatusType.DRAFT)
                                        .build();
                        Section savedSection = Section.builder()
                                        .id(sectionId)
                                        .scoreStatus(ScoreStatusType.DRAFT)
                                        .classroom(classroom)
                                        .curriculum(curriculum)
                                        .build();

                        given(classroomService.getClassroomById(classroomId)).willReturn(classroom);
                        given(curriculumService.getCurriculumById(curriculumId)).willReturn(curriculum);
                        given(sectionRepo.save(inputSection)).willReturn(savedSection);
                        // Mock userClient since handleChatGroups fetches student emails
                        given(userClient.getUsersMessages(eq(List.of(studentId)), anyMap()))
                                        .willReturn(new PageImpl<>(Collections.emptyList()));

                        // Act
                        Section result = sectionService.saveSingleSection(inputSection, classroomId, curriculumId);

                        // Assert
                        assertThat(result).isSameAs(savedSection);
                        ArgumentCaptor<ScoreSyncEvent> captor = ArgumentCaptor.forClass(ScoreSyncEvent.class);
                        then(eventPublisher).should().publishScoreSync(captor.capture());
                        ScoreSyncEvent event = captor.getValue();
                        assertThat(event.sectionIds()).containsExactly(sectionId);
                        assertThat(event.newStudentIds()).containsExactly(studentId);
                }

                @Test
                @DisplayName("Happy Path: does NOT publish ScoreSyncEvent when classroom has no students")
                void saveSingleSection_WithNoStudents_DoesNotPublishScoreSyncEvent() {
                        // Arrange (classroom.studentClassroomSet is empty by default)
                        Section inputSection = Section.builder().scoreStatus(ScoreStatusType.DRAFT).build();
                        Section savedSection = Section.builder()
                                        .id(sectionId)
                                        .scoreStatus(ScoreStatusType.DRAFT)
                                        .classroom(classroom)
                                        .curriculum(curriculum)
                                        .build();

                        given(classroomService.getClassroomById(classroomId)).willReturn(classroom);
                        given(curriculumService.getCurriculumById(curriculumId)).willReturn(curriculum);
                        given(sectionRepo.save(inputSection)).willReturn(savedSection);

                        // Act
                        sectionService.saveSingleSection(inputSection, classroomId, curriculumId);

                        // Assert
                        then(eventPublisher).should(never()).publishScoreSync(any());
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // handleChatGroups (tested indirectly via saveSingleSection)
        // ═══════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("handleChatGroups — via saveSingleSection")
        class HandleChatGroupsTests {

                @Test
                @DisplayName("Creates new chat group when teacher is assigned for the first time (oldTeacherId=null, newTeacherId≠null)")
                void handleChatGroups_WhenNewTeacherAssigned_PublishesChatGroupCreate() {
                        // Arrange
                        UUID teacherId = UUID.randomUUID();
                        String teacherEmail = "teacher@example.com";

                        Section inputSection = Section.builder()
                                        .teacherId(teacherId)
                                        .scoreStatus(ScoreStatusType.DRAFT)
                                        .build();
                        // saved section has same teacherId — existingTeacherMap from saveSingleSection
                        // is empty
                        Section savedSection = Section.builder()
                                        .id(sectionId)
                                        .teacherId(teacherId)
                                        .scoreStatus(ScoreStatusType.DRAFT)
                                        .classroom(classroom)
                                        .curriculum(curriculum)
                                        .build();

                        given(classroomService.getClassroomById(classroomId)).willReturn(classroom);
                        given(curriculumService.getCurriculumById(curriculumId)).willReturn(curriculum);
                        given(sectionRepo.save(inputSection)).willReturn(savedSection);

                        UserMessageResponseDTO teacherMsg = new UserMessageResponseDTO(
                                        "Minh", "Le", teacherEmail, null, "Teacher");
                        given(userClient.getUsersMessages(eq(List.of(teacherId)), anyMap()))
                                        .willReturn(new PageImpl<>(List.of(teacherMsg)));


                        // Act
                        sectionService.saveSingleSection(inputSection, classroomId, curriculumId);

                        // Assert
                        ArgumentCaptor<ChatGroupCreateEvent> captor = ArgumentCaptor
                                        .forClass(ChatGroupCreateEvent.class);
                        then(eventPublisher).should().publishChatGroupCreate(captor.capture());
                        ChatGroupCreateEvent event = captor.getValue();
                        assertThat(event.sectionId()).isEqualTo(sectionId);
                        assertThat(event.teacherEmail()).isEqualTo(teacherEmail);
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // deleteSection
        // ═══════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("deleteSection")
        class DeleteSectionTests {

                @Test
                @DisplayName("Happy Path: publishes ScoreSyncEvent and ChatGroupDeleteEvent, then deletes")
                void deleteSection_HappyPath_PublishesEventsAndDeletes() {
                        // Arrange (no findById needed — deleteSection does not call getSectionById)

                        // Act
                        sectionService.deleteSection(sectionId);

                        // Assert
                        ArgumentCaptor<ScoreSyncEvent> scoreSyncCaptor = ArgumentCaptor.forClass(ScoreSyncEvent.class);
                        then(eventPublisher).should().publishScoreSync(scoreSyncCaptor.capture());
                        assertThat(scoreSyncCaptor.getValue().sectionIds()).containsExactly(sectionId);

                        ArgumentCaptor<ChatGroupDeleteEvent> chatDeleteCaptor = ArgumentCaptor
                                        .forClass(ChatGroupDeleteEvent.class);
                        then(eventPublisher).should().publishChatGroupDelete(chatDeleteCaptor.capture());
                        assertThat(chatDeleteCaptor.getValue().sectionIds()).containsExactly(sectionId);

                        then(sectionRepo).should().deleteById(sectionId);
                }
        }
}
