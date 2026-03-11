package com.lqm.academic_service.dtos;

import jakarta.validation.Valid;
import lombok.Builder;

import java.util.List;

@Builder
public record SectionListRequest(
        @Valid
        List<SectionRequestDTO> sections
) {}