package com.lqm.admin_service.dtos;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AdminReplyResponseDTO(
        UUID id,
        String content,
        String ownerName,
        String postTitle,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {}
