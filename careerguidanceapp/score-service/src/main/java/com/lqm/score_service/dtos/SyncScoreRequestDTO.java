package com.lqm.score_service.dtos;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record SyncScoreRequestDTO(
        List<UUID> sectionIds,
        List<UUID> newStudentIds,
        List<UUID> removedStudentIds
) {}