package com.lqm.academic_service.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubjectConfig {
    @Value("${required.subjects}")
    private String requiredSubjectsString;

    private List<String> requiredSubjects;

    public List<String> getRequiredSubjects() {
        if (requiredSubjects == null) {
            requiredSubjects = Arrays.stream(requiredSubjectsString.split(","))
                    .map(String::trim)
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        }
        return requiredSubjects;
    }
}
