package com.lqm.score_service.services;

import java.util.UUID;

public interface SecurityService {
    boolean hasPermission(UUID targetId, String targetType, String permission);
}
