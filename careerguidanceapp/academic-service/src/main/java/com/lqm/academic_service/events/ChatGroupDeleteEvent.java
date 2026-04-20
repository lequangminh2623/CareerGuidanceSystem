package com.lqm.academic_service.events;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record ChatGroupDeleteEvent(
        List<UUID> sectionIds) implements Serializable {
}
