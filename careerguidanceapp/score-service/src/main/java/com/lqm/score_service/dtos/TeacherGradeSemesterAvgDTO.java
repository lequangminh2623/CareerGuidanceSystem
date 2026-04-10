package com.lqm.score_service.dtos;

import lombok.Builder;

@Builder
public record TeacherGradeSemesterAvgDTO(
        String semesterLabel,
        Double avgScore
) {}
