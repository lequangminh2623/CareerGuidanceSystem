package com.lqm.score_service.annotations;

import com.lqm.score_service.validators.ValidExtraScoresValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidExtraScoresValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidExtraScores {
    String message() default "Each grade must be between 0 and 10";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
