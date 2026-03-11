package com.lqm.academic_service.configs;

import com.lqm.academic_service.models.SemesterType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SemesterConfig {

    @Value("${required.semesters}")
    private String requiredSemestersString;

    private List<SemesterType> requiredSemesters;

    public List<SemesterType> getRequiredSemesters() {
        if (requiredSemesters == null) {
            requiredSemesters = Arrays.stream(requiredSemestersString.split(","))
                    .map(String::trim)
                    .map(SemesterType::fromSemesterName)
                    .toList();
        }
        return requiredSemesters;
    }
}
