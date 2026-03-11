package com.lqm.user_service.dtos;

import com.lqm.user_service.annotations.EnumValue;
import com.lqm.user_service.models.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserRequestDTO(
        UUID id,

        @Size(max = 255)
        String firstName,

        @Size(max = 255)
        String lastName,

        @NotNull(message = "{user.gender.notNull}")
        Boolean gender,

        @Size(max = 255)
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@ou\\.edu\\.vn$", message = "{user.email.invalid}")
        String email,

        @Size(max = 255)
        String password,

        @Size(min = 10, max = 10, message = "{user.student.code.size}")
        String code
) { }
