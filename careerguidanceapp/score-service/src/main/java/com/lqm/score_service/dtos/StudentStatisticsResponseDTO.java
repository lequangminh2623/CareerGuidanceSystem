package com.lqm.score_service.dtos;

import lombok.Builder;

import java.util.List;
import java.io.Serializable;

@Builder
public record StudentStatisticsResponseDTO(
        List<StudentSemesterAvgDTO> semesterAverages,
        List<StudentYearAvgDTO> yearAverages
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
