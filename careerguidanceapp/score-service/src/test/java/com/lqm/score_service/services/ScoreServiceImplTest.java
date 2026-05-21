package com.lqm.score_service.services;

import com.lqm.score_service.clients.ClassroomClient;
import com.lqm.score_service.clients.SectionClient;
import com.lqm.score_service.dtos.ClassroomDetailsResponseDTO;
import com.lqm.score_service.dtos.SectionResponseDTO;
import com.lqm.score_service.exceptions.BadRequestException;
import com.lqm.score_service.exceptions.ResourceNotFoundException;
import com.lqm.score_service.models.ExtraScore;
import com.lqm.score_service.models.ScoreDetail;
import com.lqm.score_service.repositories.ScoreDetailRepository;
import com.lqm.score_service.services.impl.ScoreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScoreServiceImpl Unit Tests")
class ScoreServiceImplTest {

    @Mock
    private SectionClient sectionClient;
    @Mock
    private ScoreDetailRepository scoreDetailRepo;
    @Mock
    private ClassroomClient classroomClient;
    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ScoreServiceImpl scoreService;

    private UUID sectionId;
    private UUID studentId;
    private UUID classroomId;

    @BeforeEach
    void setUp() {
        sectionId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        classroomId = UUID.randomUUID();

        // Inject the constant directly using reflection since @Value is not processed
        // by Mockito
        org.springframework.test.util.ReflectionTestUtils.setField(scoreService, "MAX_EXTRA_SCORES", 5);

        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    @DisplayName("saveScores()")
    class SaveScoresTests {

        @Test
        @DisplayName("Happy Path: saveScores successfully saves and balances extra scores")
        void saveScores_HappyPath_SavesSuccessfully() {
            // Arrange
            SectionResponseDTO sectionDTO = new SectionResponseDTO(
                    sectionId, classroomId, "Giáo viên A", "10A1",
                    "Grade 10", "2024-2025", "First semester", "Toán", "UNLOCKED");

            ScoreDetail request = ScoreDetail.builder()
                    .sectionId(sectionId)
                    .studentId(studentId)
                    .midtermScore(8.0)
                    .finalScore(9.0)
                    .extraScoreSet(new LinkedHashSet<>(List.of(
                            ExtraScore.builder().scoreIndex(0).score(10.0).build())))
                    .build();

            given(sectionClient.getSectionResponseById(sectionId)).willReturn(sectionDTO);
            given(classroomClient.getNonExistingStudentIds(classroomId, List.of(studentId)))
                    .willReturn(Collections.emptyList());
            given(scoreDetailRepo.findBySectionId(sectionId)).willReturn(Collections.emptyList());

            ClassroomDetailsResponseDTO classroomDetails = new ClassroomDetailsResponseDTO(classroomId, "10A1",
                    "Grade 10", "2024-2025", List.of(studentId));
            given(classroomClient.getClassroomDetailsResponseById(classroomId)).willReturn(classroomDetails);

            given(scoreDetailRepo.saveAll(any())).willAnswer(inv -> inv.getArgument(0));

            // Act
            List<ScoreDetail> result = scoreService.saveScores(List.of(request));

            // Assert
            assertThat(result).hasSize(1);
            ScoreDetail saved = result.getFirst();
            assertThat(saved.getMidtermScore()).isEqualTo(8.0);
            assertThat(saved.getFinalScore()).isEqualTo(9.0);
            assertThat(saved.getExtraScoreSet()).hasSize(1);
            verify(scoreDetailRepo).saveAll(any());
        }

        @Test
        @DisplayName("Exception Path: throws ResourceNotFoundException when student not in classroom")
        void saveScores_StudentNotInClassroom_ThrowsException() {
            // Arrange
            SectionResponseDTO sectionDTO = new SectionResponseDTO(
                    sectionId, classroomId, "Giáo viên A", "10A1",
                    "Grade 10", "2024-2025", "First semester", "Toán", "UNLOCKED");
            ScoreDetail request = ScoreDetail.builder().sectionId(sectionId).studentId(studentId).build();

            given(sectionClient.getSectionResponseById(sectionId)).willReturn(sectionDTO);
            given(classroomClient.getNonExistingStudentIds(classroomId, List.of(studentId)))
                    .willReturn(List.of(studentId)); // Student not in classroom

            // Act & Assert
            assertThatThrownBy(() -> scoreService.saveScores(List.of(request)))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(scoreDetailRepo, never()).saveAll(any());
        }

        @Test
        @DisplayName("Exception Path: throws IllegalArgumentException when exceeding MAX_EXTRA_SCORES")
        void saveScores_ExceedMaxExtraScores_ThrowsException() {
            // Arrange
            SectionResponseDTO sectionDTO = new SectionResponseDTO(
                    sectionId, classroomId, "Giáo viên A", "10A1",
                    "Grade 10", "2024-2025", "First semester", "Toán", "UNLOCKED");

            Set<ExtraScore> extraScores = new LinkedHashSet<>();
            for (int i = 0; i < 6; i++) {
                extraScores.add(ExtraScore.builder().scoreIndex(i).score(10.0).build());
            }

            ScoreDetail request = ScoreDetail.builder().sectionId(sectionId).studentId(studentId)
                    .extraScoreSet(extraScores).build();

            given(sectionClient.getSectionResponseById(sectionId)).willReturn(sectionDTO);
            given(classroomClient.getNonExistingStudentIds(classroomId, List.of(studentId)))
                    .willReturn(Collections.emptyList());

            // Act & Assert
            assertThatThrownBy(() -> scoreService.saveScores(List.of(request)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("extraScore.exceed");
        }
    }

    @Nested
    @DisplayName("syncScoresForClassroom()")
    class SyncScoresForClassroomTests {

        @Test
        @DisplayName("Happy Path: Creates empty score details for new students")
        void syncScoresForClassroom_NewStudents_CreatesEmptyScores() {
            // Arrange
            given(scoreDetailRepo.findFirstBySectionId(sectionId)).willReturn(null);
            given(scoreDetailRepo.findBySectionId(sectionId)).willReturn(Collections.emptyList());

            // Act
            scoreService.syncScoresForClassroom(List.of(sectionId), List.of(studentId), null);

            // Assert
            verify(scoreDetailRepo).saveAll(anyList());
        }

        @Test
        @DisplayName("Happy Path: Removes scores for removed students")
        void syncScoresForClassroom_RemovedStudents_DeletesScores() {
            // Act
            scoreService.syncScoresForClassroom(List.of(sectionId), null, List.of(studentId));

            // Assert
            verify(scoreDetailRepo).deleteAllBySectionIdInAndStudentIdIn(List.of(sectionId), List.of(studentId));
            verify(scoreDetailRepo, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("importScoresFromCsv()")
    class ImportScoresFromCsvTests {
        // Mocking CSV import requires setting up a dummy CSV file and mocking
        // dependencies
        @Test
        @DisplayName("Exception Path: Invalid file format -> Throws exception")
        void importScoresFromCsv_InvalidFile_ThrowsException() {
            // Arrange
            SectionResponseDTO sectionDTO = new SectionResponseDTO(
                    sectionId, classroomId, "Giáo viên A", "10A1",
                    "Grade 10", "2024-2025", "First semester", "Toán", "UNLOCKED");
            given(sectionClient.getSectionResponseById(sectionId)).willReturn(sectionDTO);

            org.springframework.data.domain.Page<com.lqm.score_service.dtos.UserResponseDTO> students = new org.springframework.data.domain.PageImpl<>(
                    List.of(
                            new com.lqm.score_service.dtos.UserResponseDTO(studentId, "HS01", "A", "Nguyen")));
            given(classroomClient.getStudentsInClassroom(eq(classroomId), any())).willReturn(students);

            String csvContent = "Mã học sinh,Họ và tên,Điểm GK,Điểm CK\n" +
                    "HS01,Nguyen B,8.0,9.0"; // Name mismatch
            MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

            // Act & Assert
            assertThatThrownBy(() -> scoreService.importScoresFromCsv(sectionId, file))
                    .isInstanceOf(BadRequestException.class);
        }
    }
}
