package com.lqm.academic_service.configs;

import com.lqm.academic_service.models.GradeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GradeConfig {
    @Value("${required.grades}")
    private String requiredGradesString;

    private List<GradeType> requiredGrades;

    public List<GradeType> getRequiredGrades() {
        if (requiredGrades == null) {
            requiredGrades = Arrays.stream(requiredGradesString.split(","))
                    .map(String::trim)
                    .map(GradeType::fromGradeName)
                    .collect(Collectors.toList());
        }
        return requiredGrades;
    }
}
