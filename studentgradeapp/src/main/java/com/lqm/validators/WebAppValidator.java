package com.lqm.validators;

import com.lqm.dtos.TranscriptDTO;
import jakarta.validation.ConstraintViolation;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class WebAppValidator implements Validator {

    private final jakarta.validation.Validator beanValidator;
    private final Map<Class<?>, Validator> validatorMap;

    @Autowired
    public WebAppValidator(jakarta.validation.Validator beanValidator, Set<Validator> validators) {
        this.beanValidator = beanValidator;
        this.validatorMap = new HashMap<>();
        for (Validator v : validators) {
            if (v instanceof SupportsClass sc) {
                validatorMap.put(sc.getSupportedClass(), v);
            }
        }
    }

    @Override
    public boolean supports(@NotNull Class<?> clazz) {
        if (TranscriptDTO.class.isAssignableFrom(clazz)
                || Page.class.isAssignableFrom(clazz)
                || List.class.isAssignableFrom(clazz)) {return true;}
        for (Validator v : validatorMap.values()) {
            if (v.supports(clazz)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void validate(@NotNull Object target, @NotNull Errors errors) {
        Set<ConstraintViolation<Object>> violations = beanValidator.validate(target);
        for (ConstraintViolation<Object> violation : violations) {
            errors.rejectValue(violation.getPropertyPath().toString(), violation.getMessageTemplate(), violation.getMessage());
        }

        Validator v = validatorMap.get(target.getClass());
        if (v != null) {
            v.validate(target, errors);
        }
    }
}
