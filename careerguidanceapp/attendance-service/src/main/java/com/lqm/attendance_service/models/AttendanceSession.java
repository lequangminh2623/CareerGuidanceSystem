package com.lqm.attendance_service.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttendanceSession {
    MORNING("Buổi sáng"),
    AFTERNOON("Buổi chiều");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
