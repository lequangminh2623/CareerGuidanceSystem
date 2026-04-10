package com.lqm.score_service.dtos;

import lombok.Builder;

@Builder
public record TeacherSectionAvgDTO(
        String sectionLabel,
        Double avgScore
) {}
