package com.lqm.academic_service.services;

import com.lqm.academic_service.configs.SubjectConfig;
import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.models.*;
import com.lqm.academic_service.repositories.CurriculumRepository;
import com.lqm.academic_service.repositories.GradeRepository;
import com.lqm.academic_service.services.Impl.GradeServiceImpl;
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

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for {@link GradeServiceImpl}.
 * Pattern: AAA (Arrange – Act – Assert).
 */
@ExtendWith(MockitoExtension.class)
class GradeServiceImplTest {

    @Mock private GradeRepository gradeRepo;
    @Mock private MessageSource messageSource;
    @Mock private CurriculumRepository curriculumRepo;
    @Mock private SemesterService semesterService;
    @Mock private SubjectConfig subjectConfig;
    @Mock private SubjectService subjectService;

    @InjectMocks
    private GradeServiceImpl gradeService;

    private UUID gradeId;
    private UUID yearId;
    private Year year;

    @BeforeEach
    void setUp() {
        gradeId = UUID.randomUUID();
        yearId  = UUID.randomUUID();
        year = Year.builder().id(yearId).name("2024-2025").build();

        org.mockito.Mockito.lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getGradeById
    // ═══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getGradeById")
    class GetGradeByIdTests {

        @Test
        @DisplayName("Happy Path: returns grade when found")
        void getGradeById_WhenFound_ReturnsGrade() {
            // Arrange
            Grade grade = Grade.builder().id(gradeId).name(GradeType.GRADE_10).year(year).build();
            given(gradeRepo.findById(gradeId)).willReturn(Optional.of(grade));

            // Act
            Grade result = gradeService.getGradeById(gradeId);

            // Assert
            assertThat(result).isSameAs(grade);
        }

        @Test
        @DisplayName("Exception Path: throws ResourceNotFoundException when grade not found")
        void getGradeById_WhenNotFound_ThrowsResourceNotFoundException() {
            // Arrange
            given(gradeRepo.findById(gradeId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> gradeService.getGradeById(gradeId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // saveGrade — Auto-initialization of Curriculum
    // ═══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("saveGrade")
    class SaveGradeTests {

        @Test
        @DisplayName("Happy Path (new grade): auto-initializes Curriculum for each required subject × each semester")
        void saveGrade_WhenNewGrade_InitializesAllCurriculumEntries() {
            // Arrange
            Grade newGrade = Grade.builder().name(GradeType.GRADE_10).build(); // id == null → isNew
            Grade savedGrade = Grade.builder().id(gradeId).name(GradeType.GRADE_10).year(year).build();

            given(gradeRepo.save(newGrade)).willReturn(savedGrade);

            Semester sem1 = Semester.builder().id(UUID.randomUUID()).name(SemesterType.SEMESTER_1).year(year).build();
            Semester sem2 = Semester.builder().id(UUID.randomUUID()).name(SemesterType.SEMESTER_2).year(year).build();
            given(semesterService.getSemestersByYearId(yearId, Map.of())).willReturn(List.of(sem1, sem2));

            List<String> requiredSubjects = List.of("Toán", "Văn", "Lý");
            given(subjectConfig.getRequiredSubjects()).willReturn(requiredSubjects);

            Subject mathSubject = Subject.builder().name("Toán").build();
            Subject litSubject  = Subject.builder().name("Văn").build();
            Subject physSubject = Subject.builder().name("Lý").build();
            given(subjectService.getSubjectByName("Toán")).willReturn(mathSubject);
            given(subjectService.getSubjectByName("Văn")).willReturn(litSubject);
            given(subjectService.getSubjectByName("Lý")).willReturn(physSubject);

            given(curriculumRepo.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));

            // Act
            Grade result = gradeService.saveGrade(newGrade, year);

            // Assert
            assertThat(result).isSameAs(savedGrade);

            // Expect: 3 subjects × 2 semesters = 6 curriculum entries
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Curriculum>> captor = ArgumentCaptor.forClass(List.class);
            then(curriculumRepo).should().saveAll(captor.capture());
            List<Curriculum> curricula = captor.getValue();
            assertThat(curricula).hasSize(6);
            // All should reference the saved grade
            assertThat(curricula).allMatch(c -> savedGrade.equals(c.getGrade()));
        }

        @Test
        @DisplayName("Happy Path (update grade): does NOT re-initialize Curriculum")
        void saveGrade_WhenExistingGrade_DoesNotInitializeCurriculum() {
            // Arrange
            Grade existingGrade = Grade.builder().id(gradeId).name(GradeType.GRADE_10).year(year).build();
            given(gradeRepo.save(existingGrade)).willReturn(existingGrade);

            // Act
            Grade result = gradeService.saveGrade(existingGrade, year);

            // Assert
            assertThat(result).isSameAs(existingGrade);
            then(curriculumRepo).should(never()).saveAll(any());
            then(subjectConfig).should(never()).getRequiredSubjects();
        }
    }
}
