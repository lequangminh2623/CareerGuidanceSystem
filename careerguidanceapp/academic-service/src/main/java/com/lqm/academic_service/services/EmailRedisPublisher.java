package com.lqm.academic_service.services;

import com.lqm.academic_service.dtos.MailMessageDTO;

public interface EmailRedisPublisher {
    void publish(MailMessageDTO message);
}
