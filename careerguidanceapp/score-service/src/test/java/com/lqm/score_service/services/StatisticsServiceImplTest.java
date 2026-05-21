package com.lqm.score_service.services;

import com.lqm.score_service.clients.SectionClient;
import com.lqm.score_service.dtos.SectionResponseDTO;
import com.lqm.score_service.dtos.StudentSemesterAvgDTO;
import com.lqm.score_service.dtos.StudentStatisticsResponseDTO;
import com.lqm.score_service.dtos.TeacherSectionAvgDTO;
import com.lqm.score_service.models.ExtraScore;
import com.lqm.score_service.models.ScoreDetail;
import com.lqm.score_service.repositories.ScoreDetailRepository;
import com.lqm.score_service.services.impl.StatisticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsServiceImpl Unit Tests")
class StatisticsServiceImplTest {

    @Mock
    private ScoreDetailRepository scoreDetailRepository;

    @Mock
    private SectionClient sectionClient;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    private UUID studentId;
    private UUID sectionIdSem1;
    private UUID sectionIdSem2;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        sectionIdSem1 = UUID.randomUUID();
        sectionIdSem2 = UUID.randomUUID();
    }

    @Nested
    @DisplayName("getStudentStatistics()")
    class GetStudentStatisticsTests {

        @Test
        @DisplayName("Happy Path: Calculate averages correctly with both semesters")
        void getStudentStatistics_WithBothSemesters_CalculatesAveragesCorrectly() {
            // Arrange
            // Create scores for Semester 1
            ScoreDetail scoreSem1 = new ScoreDetail();
            scoreSem1.setSectionId(sectionIdSem1);
            scoreSem1.setStudentId(studentId);
            scoreSem1.setMidtermScore(8.0);
            scoreSem1.setFinalScore(9.0);
            ExtraScore es1 = ExtraScore.builder().score(10.0).build();
            scoreSem1.setExtraScoreSet(Set.of(es1)); // Avg: (10 + 2*8 + 3*9) / 6 = 53 / 6 = 8.83

            // Create scores for Semester 2
            ScoreDetail scoreSem2 = new ScoreDetail();
            scoreSem2.setSectionId(sectionIdSem2);
            scoreSem2.setStudentId(studentId);
            scoreSem2.setMidtermScore(7.0);
            scoreSem2.setFinalScore(8.0);
            ExtraScore es2 = ExtraScore.builder().score(8.0).build();
            scoreSem2.setExtraScoreSet(Set.of(es2)); // Avg: (8 + 2*7 + 3*8) / 6 = 46 / 6 = 7.67

            given(scoreDetailRepository.findByStudentId(studentId)).willReturn(List.of(scoreSem1, scoreSem2));

            SectionResponseDTO sec1 = new SectionResponseDTO(sectionIdSem1, UUID.randomUUID(), "Teacher", "10A1",
                    "Grade 10", "2024-2025", "First semester", "Toán", "UNLOCKED");
            SectionResponseDTO sec2 = new SectionResponseDTO(sectionIdSem2, UUID.randomUUID(), "Teacher", "10A1",
                    "Grade 10", "2024-2025", "Second semester", "Toán", "UNLOCKED");

            given(sectionClient.getSections(anyList(), any())).willReturn(new PageImpl<>(List.of(sec1, sec2)));

            // Act
            StudentStatisticsResponseDTO result = statisticsService.getStudentStatistics(studentId);

            // Assert
            assertThat(result.semesterAverages()).hasSize(2);

            StudentSemesterAvgDTO sem1Avg = result.semesterAverages().get(0);
            assertThat(sem1Avg.semesterName()).isEqualTo("First semester");
            assertThat(sem1Avg.avgScore()).isEqualTo(8.83);

            StudentSemesterAvgDTO sem2Avg = result.semesterAverages().get(1);
            assertThat(sem2Avg.semesterName()).isEqualTo("Second semester");
            assertThat(sem2Avg.avgScore()).isEqualTo(7.67);

            assertThat(result.yearAverages()).hasSize(1);
            // Year Avg = (sem1 + 2*sem2) / 3 = (8.83 + 2*7.67) / 3 = 24.17 / 3 = 8.06
            assertThat(result.yearAverages().get(0).avgScore()).isEqualTo(8.06);
        }

        @Test
        @DisplayName("Happy Path: No scores -> Returns empty statistics")
        void getStudentStatistics_NoScores_ReturnsEmpty() {
            // Arrange
            given(scoreDetailRepository.findByStudentId(studentId)).willReturn(Collections.emptyList());

            // Act
            StudentStatisticsResponseDTO result = statisticsService.getStudentStatistics(studentId);

            // Assert
            assertThat(result.semesterAverages()).isEmpty();
            assertThat(result.yearAverages()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTeacherSectionStatistics()")
    class GetTeacherSectionStatisticsTests {

        @Test
        @DisplayName("Happy Path: Returns correct section averages for teacher")
        void getTeacherSectionStatistics_HappyPath() {
            // Arrange
            UUID teacherId = UUID.randomUUID();
            SectionResponseDTO sec1 = new SectionResponseDTO(sectionIdSem1, UUID.randomUUID(), "Teacher", "10A1",
                    "Grade 10", "2024-2025", "First semester", "Toán", "UNLOCKED");

            given(sectionClient.getSectionsByTeacherId(teacherId)).willReturn(List.of(sec1));

            ScoreDetail score = new ScoreDetail();
            score.setSectionId(sectionIdSem1);
            score.setMidtermScore(8.0);
            score.setFinalScore(9.0);
            // Avg: (0 + 16 + 27)/5 = 43/5 = 8.6

            given(scoreDetailRepository.findBySectionIdIn(List.of(sectionIdSem1))).willReturn(List.of(score));

            // Act
            List<TeacherSectionAvgDTO> result = statisticsService.getTeacherSectionStatistics(teacherId, "2024-2025");

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).avgScore()).isEqualTo(8.6);
            assertThat(result.get(0).sectionLabel()).isEqualTo("Toán - 10A1");
        }
    }
}
