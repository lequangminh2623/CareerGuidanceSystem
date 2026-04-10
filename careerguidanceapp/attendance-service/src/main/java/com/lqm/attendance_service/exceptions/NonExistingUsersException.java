package com.lqm.attendance_service.exceptions;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class NonExistingUsersException extends RuntimeException {
    private final List<UUID> nonExistingIds;

    public NonExistingUsersException(List<UUID> nonExistingIds) {
        super("The following User IDs were not found: " + nonExistingIds);
        this.nonExistingIds = nonExistingIds;
    }

}
