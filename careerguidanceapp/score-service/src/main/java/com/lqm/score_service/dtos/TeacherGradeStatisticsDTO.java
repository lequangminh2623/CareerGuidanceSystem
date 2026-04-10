package com.lqm.score_service.dtos;

import lombok.Builder;

import java.util.List;

@Builder
public record TeacherGradeStatisticsDTO(
        String gradeName,
        List<TeacherGradeSemesterAvgDTO> semesterAverages
) {}
