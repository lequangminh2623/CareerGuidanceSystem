package com.lqm.transcript_service.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GradeStatusType {
    ROLE_STUDENT("Student"),
    ROLE_TEACHER("Teacher"),
    ROLE_ADMIN("Admin");

    private final String gradeStatusName;

    public static GradeStatusType fromGradeStatusName(String gradeStatusName) {
        for (GradeStatusType gradeStatusType : values()) {
            if (gradeStatusType.gradeStatusName.equalsIgnoreCase(gradeStatusName)) {
                return gradeStatusType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return gradeStatusName;
    }
}
