package com.lqm.academic_service.validators;

import com.lqm.academic_service.dtos.YearRequestDTO;
import com.lqm.academic_service.services.YearService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@RequiredArgsConstructor
@Component
public class YearRequestDTOValidator implements Validator, SupportsClass {

    private final YearService yearService;

    public Class<?> getSupportedClass() {
        return YearRequestDTO.class;
    }

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return YearRequestDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        YearRequestDTO year = (YearRequestDTO) target;

        if (year.name() == null) {
            errors.rejectValue("name", "year.name.notNull");
        }
        if (!errors.hasFieldErrors() && yearService.existDuplicateYear(year.name(), year.id())) {
            errors.rejectValue("name", "year.unique");
        }
    }
}
