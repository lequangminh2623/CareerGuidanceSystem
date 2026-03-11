package com.lqm.admin_service.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Builder
public record AdminReplyRequestDTO(
        UUID id,
        String content,
        String image,
        MultipartFile file,
        UUID parentId,
        @NotNull(message = "reply.post.notNull")
        UUID postId,
        @NotNull(message = "reply.owner.notNull")
        UUID ownerId

) {}
