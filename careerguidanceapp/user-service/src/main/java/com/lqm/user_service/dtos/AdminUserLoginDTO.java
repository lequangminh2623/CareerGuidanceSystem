package com.lqm.user_service.dtos;

import lombok.Builder;

@Builder
public record AdminUserLoginDTO(
        String email,
        String password,
        String role
) {}
