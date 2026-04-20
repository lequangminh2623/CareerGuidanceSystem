package com.lqm.attendance_service.events;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record ClassroomDeletedEvent(
        UUID classroomId,
        List<UUID> sectionIds) implements Serializable {
}
