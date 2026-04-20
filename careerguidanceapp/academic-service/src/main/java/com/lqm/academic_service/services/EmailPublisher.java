package com.lqm.academic_service.services;

import com.lqm.academic_service.dtos.MailMessageDTO;

public interface EmailPublisher {
    void publish(MailMessageDTO message);
}
