package com.lqm.score_service.dtos;

import lombok.Builder;

import java.util.List;

@Builder
public record StudentStatisticsResponseDTO(
        List<StudentSemesterAvgDTO> semesterAverages,
        List<StudentYearAvgDTO> yearAverages
) {}
