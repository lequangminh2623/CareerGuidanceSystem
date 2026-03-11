package com.lqm.user_service.validators;

import com.lqm.user_service.dtos.AdminUserRequestDTO;
import com.lqm.user_service.models.Role;
import com.lqm.user_service.services.StudentService;
import com.lqm.user_service.services.UserService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class AdminUserRequestDTOValidator implements Validator, SupportsClass {

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
        AdminUserRequestDTO adminUserRequestDTO = (AdminUserRequestDTO) target;

        if (adminUserRequestDTO.firstName() == null || adminUserRequestDTO.firstName().trim().isEmpty()) {
            errors.rejectValue("firstName", "user.firstName.notNull");
        }

        if (adminUserRequestDTO.lastName() == null || adminUserRequestDTO.lastName().trim().isEmpty()) {
            errors.rejectValue("lastName", "user.lastName.notNull");
        }

        if (adminUserRequestDTO.email() == null || adminUserRequestDTO.email().trim().isEmpty()) {
            errors.rejectValue("email", "user.email.notNull");
        }
        if (adminUserRequestDTO.role() == null || adminUserRequestDTO.role().trim().isEmpty()) {
            errors.rejectValue("role", "user.role.notNull");
        }

        if (!errors.hasFieldErrors("email")) {
            boolean existEmail = userService.existDuplicateUser(adminUserRequestDTO.email(), adminUserRequestDTO.id());
            if (existEmail) {
                errors.rejectValue("email", "user.email.unique");
            }
        }
        if(Role.ROLE_STUDENT.getRoleName().equals(adminUserRequestDTO.role())) {
            if ((adminUserRequestDTO.code() == null || adminUserRequestDTO.code().trim().isEmpty())) {
                errors.rejectValue("code", "user.student.code.notNull");
            } else {
                boolean existCode = studentService.existDuplicateStudent(adminUserRequestDTO.code(), adminUserRequestDTO.id());
                if (existCode) {
                    errors.rejectValue("code", "user.student.code.unique");
                }
            }
        }

    }
}

