package com.lqm.admin_service.dtos;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AdminPostResponseDTO(
        UUID id,
        String title,
        String sectionName,
        String ownerName,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {}
