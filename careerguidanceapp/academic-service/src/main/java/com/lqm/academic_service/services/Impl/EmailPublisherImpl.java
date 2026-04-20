package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.configs.RabbitMQConfig;
import com.lqm.academic_service.dtos.MailMessageDTO;
import com.lqm.academic_service.services.EmailPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailPublisherImpl implements EmailPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(MailMessageDTO message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, "", message);
        log.debug("Published email message to queue: to={}", message.to());
    }
}
