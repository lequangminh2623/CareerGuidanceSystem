package com.lqm.academic_service.dtos;

import java.io.Serializable;

public record MailMessageDTO(
        String to,
        String subject,
        String body
) implements Serializable {}
