package com.lqm.admin_service.validators;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ValidExtraScoresValidatorTest {

    private ValidExtraScoresValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new ValidExtraScoresValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    @DisplayName("isValid: returns true when list is null")
    void isValid_NullList_ReturnsTrue() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    @DisplayName("isValid: returns true when all scores are valid (0.0 to 10.0)")
    void isValid_ValidScores_ReturnsTrue() {
        List<Double> scores = Arrays.asList(0.0, 5.5, 10.0, null);
        assertThat(validator.isValid(scores, context)).isTrue();
    }

    @Test
    @DisplayName("isValid: returns false when a score is less than 0")
    void isValid_ScoreLessThanZero_ReturnsFalse() {
        List<Double> scores = Arrays.asList(5.0, -0.1, 8.0);
        assertThat(validator.isValid(scores, context)).isFalse();
    }

    @Test
    @DisplayName("isValid: returns false when a score is greater than 10")
    void isValid_ScoreGreaterThanTen_ReturnsFalse() {
        List<Double> scores = Arrays.asList(5.0, 10.1, 8.0);
        assertThat(validator.isValid(scores, context)).isFalse();
    }

    @Test
    @DisplayName("isValid: returns true when list is empty")
    void isValid_EmptyList_ReturnsTrue() {
        assertThat(validator.isValid(List.of(), context)).isTrue();
    }
}
