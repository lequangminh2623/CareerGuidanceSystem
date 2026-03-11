package com.lqm.score_service.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    ROLE_STUDENT("Student"),
    ROLE_TEACHER("Teacher"),
    ROLE_ADMIN("Admin");

    private final String roleName;

    public static Role fromRoleName(String roleName) {
        for (Role role : values()) {
            if (role.roleName.equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return roleName;
    }
}
