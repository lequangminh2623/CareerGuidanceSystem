package com.lqm.academic_service.events;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record ChatGroupCreateEvent(
        UUID sectionId,
        String groupName,
        String teacherEmail,
        List<String> studentEmails) implements Serializable {
}
