package com.lqm.score_service.validators;

import com.lqm.score_service.clients.UserClient;
import com.lqm.score_service.dtos.ScoreRequestDTO;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ScoreRequestDTOValidator implements Validator, SupportsClass {

    private final UserClient userClient;

    @Override
    public Class<?> getSupportedClass() {
        return ScoreRequestDTO.class;
    }

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return ScoreRequestDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        ScoreRequestDTO scoreRequestDTO = (ScoreRequestDTO) target;

        if(!userClient.checkStudentExistById(scoreRequestDTO.getStudentId())) {
            errors.rejectValue("studentId", "student.notFound");
        }
    }
}