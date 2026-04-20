package com.lqm.academic_service.events;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record ScoreSyncEvent(
        List<UUID> sectionIds,
        List<UUID> newStudentIds,
        List<UUID> removedStudentIds) implements Serializable {
}
