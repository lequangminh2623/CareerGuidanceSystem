package com.lqm.admin_service.exceptions;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private final Object details;

    public ValidationException(String message, Object details) {
        super(message);
        this.details = details;
    }
}