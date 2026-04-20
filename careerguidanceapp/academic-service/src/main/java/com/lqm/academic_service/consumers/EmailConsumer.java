package com.lqm.academic_service.consumers;

import com.lqm.academic_service.configs.RabbitMQConfig;
import com.lqm.academic_service.dtos.MailMessageDTO;
import com.lqm.academic_service.utils.MailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final MailUtils mailUtils;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void consumeMessage(MailMessageDTO message) {
        try {
            log.info("Processing mail message to: {}", message.to());
            mailUtils.sendEmail(message.to(), message.subject(), message.body());
        } catch (Exception e) {
            log.error("Error sending email to {}: {}", message.to(), e.getMessage());
        }
    }
}
