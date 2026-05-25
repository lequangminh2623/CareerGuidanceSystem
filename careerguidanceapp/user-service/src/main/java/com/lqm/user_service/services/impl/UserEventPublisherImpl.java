package com.lqm.user_service.services.impl;

import com.lqm.user_service.configs.RabbitMQConfig;
import com.lqm.user_service.events.UserDeletedEvent;
import com.lqm.user_service.services.UserEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisherImpl implements UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishUserDeleted(UserDeletedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EVENTS_EXCHANGE, RabbitMQConfig.RK_USER_DELETED, event);
        log.info("Published UserDeletedEvent: userId={}, role={}", event.userId(), event.role());
    }
}
