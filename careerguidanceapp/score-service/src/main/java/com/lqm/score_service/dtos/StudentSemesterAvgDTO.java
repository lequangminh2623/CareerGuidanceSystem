package com.lqm.score_service.dtos;

import lombok.Builder;

@Builder
public record StudentSemesterAvgDTO(
        String semesterLabel,
        String yearName,
        String semesterName,
        Double avgScore
) {}
