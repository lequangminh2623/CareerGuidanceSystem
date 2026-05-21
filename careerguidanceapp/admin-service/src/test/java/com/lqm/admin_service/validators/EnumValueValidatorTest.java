package com.lqm.admin_service.validators;

import com.lqm.admin_service.annotations.EnumValue;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class EnumValueValidatorTest {

    private EnumValueValidator validator;
    private ConstraintValidatorContext context;

    private enum TestEnum {
        ACTIVE, INACTIVE
    }

    @BeforeEach
    void setUp() {
        validator = new EnumValueValidator();
        context = mock(ConstraintValidatorContext.class);

        EnumValue enumValueAnnotation = mock(EnumValue.class);
        doReturn(TestEnum.class).when(enumValueAnnotation).enumClass();
        validator.initialize(enumValueAnnotation);
    }

    @Test
    @DisplayName("isValid: returns true when value matches enum (case insensitive)")
    void isValid_MatchesEnum_ReturnsTrue() {
        assertThat(validator.isValid("ACTIVE", context)).isTrue();
        assertThat(validator.isValid("active", context)).isTrue();
        assertThat(validator.isValid("INACTIVE", context)).isTrue();
    }

    @Test
    @DisplayName("isValid: returns true when value is null")
    void isValid_NullValue_ReturnsTrue() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    @DisplayName("isValid: returns false when value does not match enum")
    void isValid_DoesNotMatchEnum_ReturnsFalse() {
        assertThat(validator.isValid("INVALID", context)).isFalse();
        assertThat(validator.isValid("ACTIV", context)).isFalse();
        assertThat(validator.isValid("", context)).isFalse();
    }
}
