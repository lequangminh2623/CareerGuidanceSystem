package com.lqm.user_service.annotations;

import com.lqm.user_service.validators.MultipartNotNullValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MultipartNotNullValidator.class)
public @interface MultipartNotNull {
    String message() default "{error}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

