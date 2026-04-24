package com.lqm.score_service.dtos;

import lombok.Builder;
import java.io.Serializable;

@Builder
public record TeacherGradeSemesterAvgDTO(
        String semesterLabel,
        Double avgScore
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
