package com.lqm.score_service.dtos;

import lombok.Builder;
import java.io.Serializable;

@Builder
public record StudentSemesterAvgDTO(
        String semesterLabel,
        String yearName,
        String semesterName,
        Double avgScore
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
