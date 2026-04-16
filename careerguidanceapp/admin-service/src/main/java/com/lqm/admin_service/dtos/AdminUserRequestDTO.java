package com.lqm.admin_service.dtos;

import com.lqm.admin_service.annotations.EnumValue;
import com.lqm.admin_service.models.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record AdminUserRequestDTO (

        UUID id,

        @Size(max = 255)
        @NotBlank(message = "{user.firstName.notNull}")
        String firstName,

        @Size(max = 255)
        @NotBlank(message = "{user.lastName.notNull}")
        String lastName,

        @NotNull(message = "{user.gender.notNull}")
        Boolean gender,

        @Size(max = 255)
        @NotBlank(message = "{user.email.notNull}")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@ou\\.edu\\.vn$", message = "{user.email.invalid}")
        String email,

        @EnumValue(enumClass = Role.class, message = "{user.role.invalid}")
        @Size(max = 13)
        @NotBlank(message = "{user.role.notNull}")
        String role,

        @Size(min = 10, max = 10, message = "{user.student.code.size}")
        String code,

        @NotNull(message = "{user.active.notNull}")
        Boolean active,

        @Size(max = 255)
        String avatar

) {}
