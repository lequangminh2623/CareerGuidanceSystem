package com.lqm.attendance_service.validators;

import jakarta.annotation.Nonnull;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WebAppValidator implements Validator {

    private final Map<Class<?>, Validator> validatorMap;

    public WebAppValidator(Set<Validator> validators) {
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
        Validator v = validatorMap.get(target.getClass());
        if (v != null) {
            v.validate(target, errors);
        }
    }
}
