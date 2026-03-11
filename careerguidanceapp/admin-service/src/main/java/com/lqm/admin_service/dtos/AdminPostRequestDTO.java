package com.lqm.admin_service.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Builder
public record AdminPostRequestDTO(
   UUID id,
   String title,
   String content,
   String image,
   MultipartFile file,
   @NotNull(message = "post.section.notNull")
   UUID sectionId,
   @NotNull(message = "post.owner.notNull")
   UUID ownerId
) {}
