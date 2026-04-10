package com.lqm.score_service.dtos;

import lombok.Builder;

@Builder
public record StudentYearAvgDTO(
        String yearName,
        Double avgScore
) {}
