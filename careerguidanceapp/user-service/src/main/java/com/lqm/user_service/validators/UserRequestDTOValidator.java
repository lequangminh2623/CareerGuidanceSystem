package com.lqm.user_service.validators;

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
        return UserRequestDTO.class;
    }

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return UserRequestDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        UserRequestDTO userRequestDTO = (UserRequestDTO) target;

        if (userRequestDTO.getFirstName() == null || userRequestDTO.getFirstName().trim().isEmpty()) {
            errors.rejectValue("firstName", "user.firstName.notNull");
        }

        if (userRequestDTO.getLastName() == null || userRequestDTO.getLastName().trim().isEmpty()) {
            errors.rejectValue("lastName", "user.lastName.notNull");
        }

        if (userRequestDTO.getEmail() == null || userRequestDTO.getEmail().trim().isEmpty()) {
            errors.rejectValue("email", "user.email.notNull");
        }
        if (userRequestDTO.getRole() == null || userRequestDTO.getRole().trim().isEmpty()) {
            errors.rejectValue("role", "user.role.notNull");
        }

        if (!errors.hasFieldErrors("email")) {
            boolean existEmail = userService.existDuplicateUser(userRequestDTO.getEmail(), null);
            if (existEmail) {
                errors.rejectValue("email", "user.email.unique");
            }
        }

        if (userRequestDTO.getPassword() == null || userRequestDTO.getPassword().trim().isEmpty()) {
            errors.rejectValue("password", "user.password.notNull");
        }

        if (userRequestDTO.getCode() == null || userRequestDTO.getCode().trim().isEmpty()) {
            errors.rejectValue("code", "user.student.code.notNull");
        } else {
            boolean existCode = studentService.existDuplicateStudent(userRequestDTO.getCode(), null);
            if (existCode) {
                errors.rejectValue("code", "user.student.code.unique");
            }
        }

    }
}

