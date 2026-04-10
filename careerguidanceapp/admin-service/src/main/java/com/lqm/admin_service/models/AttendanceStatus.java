package com.lqm.admin_service.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttendanceStatus {
    PRESENT("Present"),
    ABSENT("Absent"),
    LATE("Late");

    private final String name;

    public static AttendanceStatus fromStatusName(String statusName) {
        for (AttendanceStatus a : AttendanceStatus.values()) {
            if (a.name.equalsIgnoreCase(statusName)) {
                return a;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
