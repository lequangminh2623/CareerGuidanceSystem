package com.lqm.score_service.dtos;

import lombok.Builder;

import java.util.List;
import java.io.Serializable;

@Builder
public record TeacherGradeStatisticsDTO(
        String gradeName,
        List<TeacherGradeSemesterAvgDTO> semesterAverages
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
