package com.lqm.user_service.validators;

import com.lqm.user_service.annotations.MultipartNotNull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class MultipartNotNullValidator
        implements ConstraintValidator<MultipartNotNull, MultipartFile> {

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext ctx) {
        return file != null && !file.isEmpty();
    }
}
