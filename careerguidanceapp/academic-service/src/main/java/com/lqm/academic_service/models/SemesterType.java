package com.lqm.academic_service.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter

@AllArgsConstructor
public enum SemesterType {
    SEMESTER_1("First semester"),
    SEMESTER_2("Second semester");

    private final String semesterName;

    public static SemesterType fromSemesterName(String semesterName) {
        for (SemesterType s : SemesterType.values()) {
            if (s.semesterName.equalsIgnoreCase(semesterName)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return semesterName;
    }
}
