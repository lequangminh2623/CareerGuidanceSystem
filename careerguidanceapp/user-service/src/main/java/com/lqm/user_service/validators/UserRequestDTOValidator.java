package com.lqm.user_service.validators;

import com.lqm.user_service.dtos.AdminUserRequestDTO;
import com.lqm.user_service.dtos.UserRequestDTO;
import com.lqm.user_service.services.StudentService;
import com.lqm.user_service.services.UserService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class UserRequestDTOValidator implements Validator, SupportsClass {

    private final UserService userService;
    private final StudentService studentService;

    @Override
    public Class<?> getSupportedClass() {
        return AdminUserRequestDTO.class;
    }

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return AdminUserRequestDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        UserRequestDTO dto = (UserRequestDTO) target;

        if (dto.firstName() == null || dto.firstName().trim().isEmpty()) {
            errors.rejectValue("firstName", "user.firstName.notNull");
        }

        if (dto.lastName() == null || dto.lastName().trim().isEmpty()) {
            errors.rejectValue("lastName", "user.lastName.notNull");
        }

        if (dto.email() == null || dto.email().trim().isEmpty()) {
            errors.rejectValue("email", "user.email.notNull");
        }

        if (dto.password() == null || dto.password().trim().isEmpty()) {
            errors.rejectValue("password", "user.password.notNull");
        }

        if (!errors.hasFieldErrors("email")) {
            boolean existEmail = userService.existDuplicateUser(dto.email(), dto.id());
            if (existEmail) {
                errors.rejectValue("email", "user.email.unique");
            }
        }
        if ((dto.code() == null || dto.code().trim().isEmpty())) {
            errors.rejectValue("code", "user.student.code.notNull");
        } else {
            boolean existCode = studentService.existDuplicateStudent(dto.code(), dto.id());
            if (existCode) {
                errors.rejectValue("code", "user.student.code.unique");
            }
        }


    }
}
