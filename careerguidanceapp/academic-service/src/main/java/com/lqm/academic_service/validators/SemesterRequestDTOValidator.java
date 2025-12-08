package com.lqm.academic_service.validators;

import com.lqm.academic_service.dtos.SemesterRequestDTO;
import com.lqm.academic_service.models.SemesterType;
import com.lqm.academic_service.services.SemesterService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SemesterRequestDTOValidator implements Validator, SupportsClass {

    private final SemesterService semesterService;

    @Override
    public Class<?> getSupportedClass() {
        return SemesterRequestDTO.class;
    }

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return SemesterRequestDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        SemesterRequestDTO SemesterRequestDTO = (SemesterRequestDTO) target;

        if (SemesterRequestDTO.name() == null || SemesterRequestDTO.name().trim().isEmpty()) {
            errors.rejectValue("name", "semester.name.notNull");

        }

        if (!errors.hasFieldErrors()
                && semesterService.existDuplicateSemester(SemesterType.fromSemesterName(SemesterRequestDTO.name()),
                    SemesterRequestDTO.id(), SemesterRequestDTO.yearId())) {
            errors.rejectValue("name", "semester.unique");
        }
    }
}
