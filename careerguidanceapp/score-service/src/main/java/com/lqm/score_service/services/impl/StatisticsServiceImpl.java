package com.lqm.score_service.services.impl;

import com.lqm.score_service.clients.SectionClient;
import com.lqm.score_service.dtos.*;
import com.lqm.score_service.models.ExtraScore;
import com.lqm.score_service.models.ScoreDetail;
import com.lqm.score_service.repositories.ScoreDetailRepository;
import com.lqm.score_service.services.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final ScoreDetailRepository scoreDetailRepository;
    private final SectionClient sectionClient;

    @Override
    @Cacheable(value = "score::student-stats", key = "#studentId")
    public StudentStatisticsResponseDTO getStudentStatistics(UUID studentId) {
        List<ScoreDetail> scores = scoreDetailRepository.findByStudentId(studentId);
        if (scores.isEmpty()) {
            return StudentStatisticsResponseDTO.builder()
                    .semesterAverages(List.of())
                    .yearAverages(List.of())
                    .build();
        }

        // Get section info for all scores
        List<UUID> sectionIds = scores.stream().map(ScoreDetail::getSectionId).distinct().toList();
        Map<UUID, SectionResponseDTO> sectionMap = fetchSectionMap(sectionIds);

        // Group scores by semester (yearName + semesterName)
        Map<String, List<ScoreDetail>> bySemester = new LinkedHashMap<>();
        for (ScoreDetail sd : scores) {
            SectionResponseDTO section = sectionMap.get(sd.getSectionId());
            if (section == null) continue;
            String key = section.yearName() + "|" + section.semesterName();
            bySemester.computeIfAbsent(key, k -> new ArrayList<>()).add(sd);
        }

        // Compute ĐTBhK per semester
        List<StudentSemesterAvgDTO> semesterAverages = new ArrayList<>();
        Map<String, Map<String, Double>> yearSemesterAvg = new LinkedHashMap<>();

        for (Map.Entry<String, List<ScoreDetail>> entry : bySemester.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String yearName = parts[0];
            String semesterName = parts[1];
            List<ScoreDetail> semScores = entry.getValue();

            List<Double> subjectAvgs = new ArrayList<>();
            for (ScoreDetail sd : semScores) {
                Double subjectAvg = computeSubjectAverage(sd);
                if (subjectAvg != null) {
                    subjectAvgs.add(subjectAvg);
                }
            }

            if (!subjectAvgs.isEmpty()) {
                double dtbhk = subjectAvgs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                dtbhk = Math.round(dtbhk * 100.0) / 100.0;

                semesterAverages.add(StudentSemesterAvgDTO.builder()
                        .semesterLabel(semesterName + " (" + yearName + ")")
                        .yearName(yearName)
                        .semesterName(semesterName)
                        .avgScore(dtbhk)
                        .build());

                yearSemesterAvg.computeIfAbsent(yearName, k -> new LinkedHashMap<>())
                        .put(semesterName, dtbhk);
            }
        }

        // Sort semester averages chronologically
        semesterAverages.sort(Comparator.comparing(StudentSemesterAvgDTO::yearName)
                .thenComparing(StudentSemesterAvgDTO::semesterName));

        // Compute ĐTBnăm = (ĐTBhK_sem1 + 2 * ĐTBhK_sem2) / 3
        List<StudentYearAvgDTO> yearAverages = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> entry : yearSemesterAvg.entrySet()) {
            String yearName = entry.getKey();
            Map<String, Double> sems = entry.getValue();

            Double sem1 = sems.get("First semester");
            Double sem2 = sems.get("Second semester");

            double dtbNam;
            if (sem1 != null && sem2 != null) {
                dtbNam = (sem1 + 2 * sem2) / 3.0;
            } else if (sem1 != null) {
                dtbNam = sem1;
            } else if (sem2 != null) {
                dtbNam = sem2;
            } else {
                continue;
            }
            dtbNam = Math.round(dtbNam * 100.0) / 100.0;

            yearAverages.add(StudentYearAvgDTO.builder()
                    .yearName(yearName)
                    .avgScore(dtbNam)
                    .build());
        }
        yearAverages.sort(Comparator.comparing(StudentYearAvgDTO::yearName));

        return StudentStatisticsResponseDTO.builder()
                .semesterAverages(semesterAverages)
                .yearAverages(yearAverages)
                .build();
    }

    @Override
    @Cacheable(value = "score::teacher-section-stats",
            key = "#teacherId + '_' + (#yearName != null ? #yearName : 'latest')")
    public List<TeacherSectionAvgDTO> getTeacherSectionStatistics(UUID teacherId, String yearName) {
        List<SectionResponseDTO> allSections = sectionClient.getSectionsByTeacherId(teacherId);

        if (allSections.isEmpty()) {
            return List.of();
        }

        // If no year specified, use the latest year
        if (yearName == null || yearName.isBlank()) {
            yearName = allSections.stream()
                    .map(SectionResponseDTO::yearName)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
        }

        if (yearName == null) {
            return List.of();
        }

        // Filter sections for the target year
        String finalYearName = yearName;
        List<SectionResponseDTO> yearSections = allSections.stream()
                .filter(s -> finalYearName.equals(s.yearName()))
                .toList();

        if (yearSections.isEmpty()) {
            return List.of();
        }

        List<UUID> sectionIds = yearSections.stream().map(SectionResponseDTO::id).toList();
        List<ScoreDetail> allScores = scoreDetailRepository.findBySectionIdIn(sectionIds);
        Map<UUID, List<ScoreDetail>> scoresBySection = allScores.stream()
                .collect(Collectors.groupingBy(ScoreDetail::getSectionId));

        List<TeacherSectionAvgDTO> result = new ArrayList<>();
        for (SectionResponseDTO section : yearSections) {
            List<ScoreDetail> sectionScores = scoresBySection.getOrDefault(section.id(), List.of());
            if (sectionScores.isEmpty()) continue;

            List<Double> studentAvgs = new ArrayList<>();
            for (ScoreDetail sd : sectionScores) {
                Double avg = computeSubjectAverage(sd);
                if (avg != null) studentAvgs.add(avg);
            }

            if (!studentAvgs.isEmpty()) {
                double avgScore = studentAvgs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                avgScore = Math.round(avgScore * 100.0) / 100.0;
                String label = section.subjectName() + " - " + section.classroomName();
                result.add(TeacherSectionAvgDTO.builder()
                        .sectionLabel(label)
                        .avgScore(avgScore)
                        .build());
            }
        }

        return result;
    }

    @Override
    @Cacheable(value = "score::teacher-grade-stats",
            key = "#teacherId + '_' + (#subjectName != null ? #subjectName : 'all')")
    public List<TeacherGradeStatisticsDTO> getTeacherGradeStatistics(UUID teacherId, String subjectName) {
        List<SectionResponseDTO> allSections = sectionClient.getSectionsByTeacherId(teacherId);

        if (allSections.isEmpty()) {
            return List.of();
        }

        // Filter by subject if specified
        List<SectionResponseDTO> filteredSections = allSections;
        if (subjectName != null && !subjectName.isBlank()) {
            filteredSections = allSections.stream()
                    .filter(s -> subjectName.equals(s.subjectName()))
                    .toList();
        }

        if (filteredSections.isEmpty()) {
            return List.of();
        }

        List<UUID> sectionIds = filteredSections.stream().map(SectionResponseDTO::id).toList();
        List<ScoreDetail> allScores = scoreDetailRepository.findBySectionIdIn(sectionIds);
        Map<UUID, List<ScoreDetail>> scoresBySection = allScores.stream()
                .collect(Collectors.groupingBy(ScoreDetail::getSectionId));

        // Group by gradeName -> semesterLabel -> list of subject averages
        Map<String, Map<String, List<Double>>> gradeData = new TreeMap<>();

        for (SectionResponseDTO section : filteredSections) {
            List<ScoreDetail> sectionScores = scoresBySection.getOrDefault(section.id(), List.of());
            String gradeName = section.gradeName();
            String semLabel = section.semesterName() + " (" + section.yearName() + ")";
            String sortKey = section.yearName() + "|" + section.semesterName();

            for (ScoreDetail sd : sectionScores) {
                Double avg = computeSubjectAverage(sd);
                if (avg != null) {
                    gradeData.computeIfAbsent(gradeName, k -> new TreeMap<>())
                            .computeIfAbsent(sortKey + "###" + semLabel, k -> new ArrayList<>())
                            .add(avg);
                }
            }
        }

        List<TeacherGradeStatisticsDTO> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<Double>>> gradeEntry : gradeData.entrySet()) {
            List<TeacherGradeSemesterAvgDTO> semAverages = new ArrayList<>();
            for (Map.Entry<String, List<Double>> semEntry : gradeEntry.getValue().entrySet()) {
                String[] keyParts = semEntry.getKey().split("###");
                String semLabel = keyParts.length > 1 ? keyParts[1] : keyParts[0];
                List<Double> avgs = semEntry.getValue();
                double avg = avgs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                avg = Math.round(avg * 100.0) / 100.0;
                semAverages.add(TeacherGradeSemesterAvgDTO.builder()
                        .semesterLabel(semLabel)
                        .avgScore(avg)
                        .build());
            }
            result.add(TeacherGradeStatisticsDTO.builder()
                    .gradeName(gradeEntry.getKey())
                    .semesterAverages(semAverages)
                    .build());
        }

        return result;
    }

    @Override
    public List<SubjectResponseDTO> getAllSubjects() {
        return sectionClient.getAllSubjects();
    }

    // ===== Helper methods =====

    /**
     * Compute ĐTBmHK for a single subject/scoreDetail:
     * (sum(TX) + 2*midterm + 3*final) / (count(TX) + 5)
     */
    private Double computeSubjectAverage(ScoreDetail sd) {
        if (sd.getMidtermScore() == null || sd.getFinalScore() == null) {
            return null;
        }

        double sumTX = 0;
        int countTX = 0;
        if (sd.getExtraScoreSet() != null) {
            for (ExtraScore es : sd.getExtraScoreSet()) {
                if (es.getScore() != null) {
                    sumTX += es.getScore();
                    countTX++;
                }
            }
        }

        return (sumTX + 2 * sd.getMidtermScore() + 3 * sd.getFinalScore()) / (countTX + 5);
    }

    private Map<UUID, SectionResponseDTO> fetchSectionMap(List<UUID> sectionIds) {
        var sections = sectionClient.getSections(sectionIds, Map.of("page", ""));
        return sections.getContent().stream()
                .collect(Collectors.toMap(SectionResponseDTO::id, s -> s));
    }
}
