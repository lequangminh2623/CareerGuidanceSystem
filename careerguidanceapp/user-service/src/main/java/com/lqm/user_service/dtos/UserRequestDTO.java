package com.lqm.user_service.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserRequestDTO(
                UUID id,

                @Size(max = 255, message = "{user.firstName.size}") String firstName,

                @Size(max = 255, message = "{user.lastName.size}") String lastName,

                @NotNull(message = "{user.gender.notNull}") Boolean gender,

                @Size(max = 255, message = "{user.email.size}") @Pattern(regexp = "^[A-Za-z0-9._%+-]+@ou\\.edu\\.vn$", message = "{user.email.invalid}") String email,

                @Size(max = 255, message = "{user.password.size}") String password,

                @Size(min = 10, max = 10, message = "{user.student.code.size}") String code) {
}
