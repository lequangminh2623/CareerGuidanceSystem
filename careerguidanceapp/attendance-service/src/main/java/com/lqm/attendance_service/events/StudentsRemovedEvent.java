package com.lqm.attendance_service.events;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record StudentsRemovedEvent(
        UUID classroomId,
        List<UUID> removedStudentIds) implements Serializable {
}
