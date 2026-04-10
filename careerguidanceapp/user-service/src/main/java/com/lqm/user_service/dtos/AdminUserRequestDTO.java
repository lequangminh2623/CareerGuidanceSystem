package com.lqm.user_service.dtos;

import com.lqm.user_service.annotations.EnumValue;
import com.lqm.user_service.models.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record AdminUserRequestDTO(

                UUID id,

                @Size(max = 255, message = "{user.firstName.size}") String firstName,

                @Size(max = 255, message = "{user.lastName.size}") String lastName,

                @NotNull(message = "{user.gender.notNull}") Boolean gender,

                @Size(max = 255, message = "{user.email.size}") @Pattern(regexp = "^[A-Za-z0-9._%+-]+@ou\\.edu\\.vn$", message = "{user.email.invalid}") String email,

                @EnumValue(enumClass = Role.class, message = "{user.role.invalid}") @Size(max = 13, message = "user.role.size") String role,

                @Size(min = 10, max = 10, message = "{user.student.code.size}") String code,

                @NotNull(message = "{user.active.notNull}") Boolean active,

                @Size(max = 255, message = "{user.avatar.size}") String avatar

) {
}
