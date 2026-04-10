package com.lqm.academic_service.services;

import java.util.UUID;

public interface SecurityService {
    boolean hasPermission(UUID targetId, String targetType, String permission);
}
