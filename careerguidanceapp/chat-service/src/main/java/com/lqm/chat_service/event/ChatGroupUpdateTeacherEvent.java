package com.lqm.chat_service.event;

import java.io.Serializable;
import java.util.UUID;

public record ChatGroupUpdateTeacherEvent(
        UUID sectionId,
        String oldTeacherEmail,
        String newTeacherEmail) implements Serializable {
}
