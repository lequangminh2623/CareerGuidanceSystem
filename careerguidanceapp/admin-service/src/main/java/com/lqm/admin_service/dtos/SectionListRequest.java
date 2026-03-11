package com.lqm.admin_service.dtos;

import jakarta.validation.Valid;

import java.util.List;

public record SectionListRequest(
        @Valid
        List<SectionRequestDTO> sections
) {}