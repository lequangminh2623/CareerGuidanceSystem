package com.lqm.user_service.dtos;

import com.lqm.user_service.annotations.EnumValue;
import com.lqm.user_service.models.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

        private UUID id;

        @Size(max = 255)
        private String firstName;

        @Size(max = 255)
        private String lastName;

        @NotNull(message = "{user.gender.notNull}")
        private Boolean gender;

        @Size(max = 255)
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@ou\\.edu\\.vn$", message = "{user.email.invalid}")
        private String email;

        @Size(max = 255)
        private String password;

        @EnumValue(enumClass = Role.class, message = "user.role.invalid")
        @Size(max = 13)
        private String role;

        @Size(min = 10, max = 10, message = "{user.student.code.size}")
        private String code;

        @NotNull(message = "{user.active.notNull}")
        private Boolean active;

        @Size(max = 255)
        private String avatar;

        @NotNull(message = "{user.createdDate.notNull}")
        private LocalDateTime createdDate;
}
