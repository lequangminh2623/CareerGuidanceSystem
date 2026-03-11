package com.lqm.admin_service.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GradeType {
    GRADE_10("Grade 10"),
    GRADE_11("Grade 11"),
    GRADE_12("Grade 12");

    private final String gradeName;

    public static GradeType fromGradeName(String gradeName) {
        for (GradeType g : GradeType.values()) {
            if (g.gradeName.equalsIgnoreCase(gradeName)) {
                return g;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return gradeName;
    }
}
