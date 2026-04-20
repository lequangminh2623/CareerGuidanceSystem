package com.lqm.chat_service.event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record ChatGroupCreateEvent(
        UUID sectionId,
        String groupName,
        String teacherEmail,
        List<String> studentEmails) implements Serializable {
}
