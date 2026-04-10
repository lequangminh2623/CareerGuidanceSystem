package com.lqm.user_service.dtos;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserLoginDTO(
                @Size(max = 255, message = "{user.email.size}") @Pattern(regexp = "^[A-Za-z0-9._%+-]+@ou\\.edu\\.vn$", message = "{user.email.invalid}") String email,
                @Size(max = 255, message = "{user.password.size}") String password) {
}
