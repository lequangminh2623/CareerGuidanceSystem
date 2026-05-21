package com.lqm.user_service.validators;

import jakarta.annotation.Nonnull;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

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
        if (Page.class.isAssignableFrom(clazz)
                || List.class.isAssignableFrom(clazz)) {
            return true;
        }
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
