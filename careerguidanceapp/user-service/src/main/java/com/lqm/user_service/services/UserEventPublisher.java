package com.lqm.user_service.services;

import com.lqm.user_service.events.UserDeletedEvent;

public interface UserEventPublisher {
    void publishUserDeleted(UserDeletedEvent event);
}
