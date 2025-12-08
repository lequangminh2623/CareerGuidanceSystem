package com.lqm.academic_service.validators;

import com.lqm.academic_service.dtos.SubjectRequestDTO;
import com.lqm.academic_service.services.SubjectService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SubjectRequestDTOValidator implements Validator, SupportsClass{

    private final SubjectService subjectService;

    @Override
    public Class<?> getSupportedClass() {
        return SubjectRequestDTO.class;
    }

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return SubjectRequestDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        SubjectRequestDTO subjectRequestDTO = (SubjectRequestDTO) target;
        if (subjectRequestDTO.name() == null || subjectRequestDTO.name().trim().isEmpty()) {
            errors.rejectValue("name", "subject.name.notNull");
        }
        if (!errors.hasFieldErrors() && subjectService.existDuplicateSubject(
                subjectRequestDTO.name(), subjectRequestDTO.id())) {
            errors.rejectValue("name", "subject.unique");
        }
    }
}
