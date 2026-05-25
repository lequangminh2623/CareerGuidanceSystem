package com.lqm.user_service.events;

import java.util.UUID;

public record UserDeletedEvent(UUID userId, String role) {
}
