package com.lqm.user_service.validators;

import com.lqm.user_service.dtos.UserLoginDTO;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserLoginDTOValidator implements Validator, SupportsClass {

    @Override
    public Class<?> getSupportedClass() {
        return UserLoginDTO.class;
    }

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return UserLoginDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        UserLoginDTO userLoginDTO = (UserLoginDTO) target;

        if (userLoginDTO.email() == null || userLoginDTO.email().trim().isEmpty()) {
            errors.rejectValue("email", "user.email.notNull");
        }

        if (userLoginDTO.password() == null || userLoginDTO.password().trim().isEmpty()) {
            errors.rejectValue("password", "user.password.notNull");
        }
    }

}


