package com.lqm.academic_service.validators;

import com.lqm.academic_service.dtos.GradeRequestDTO;
import com.lqm.academic_service.models.GradeType;
import com.lqm.academic_service.services.GradeService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class GradeRequestDTOValidator implements Validator, SupportsClass {

    private final GradeService gradeService;

    @Override
    public Class<?> getSupportedClass() {
        return GradeRequestDTO.class;
    }

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return GradeRequestDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        GradeRequestDTO gradeRequestDTO = (GradeRequestDTO) target;

        if (gradeRequestDTO.name() == null || gradeRequestDTO.name().trim().isEmpty()) {
            errors.rejectValue("name", "grade.name.notNull");
        }

        if (!errors.hasFieldErrors() && gradeService.existDuplicateGrade(GradeType.fromGradeName(gradeRequestDTO.name()),
                gradeRequestDTO.id(), gradeRequestDTO.yearId())) {
            errors.rejectValue("name", "grade.unique");
        }
    }
}
