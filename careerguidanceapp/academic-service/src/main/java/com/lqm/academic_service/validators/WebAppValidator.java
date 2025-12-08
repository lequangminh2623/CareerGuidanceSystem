package com.lqm.academic_service.validators;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintViolation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    public boolean supports(@Nonnull Class<?> clazz) {
        for (Validator v : validatorMap.values()) {
            if (v.supports(clazz)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
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
