package com.lqm.admin_service.dtos;

public record AdminUserLoginDTO(
        String email,
        String password,
        String role
) {}
