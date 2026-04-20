package com.lqm.academic_service.events;

import java.io.Serializable;
import java.util.UUID;

public record ChatGroupUpdateTeacherEvent(
        UUID sectionId,
        String oldTeacherEmail,
        String newTeacherEmail) implements Serializable {
}
