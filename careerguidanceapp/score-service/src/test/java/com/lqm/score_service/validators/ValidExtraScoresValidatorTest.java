package com.lqm.score_service.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidExtraScoresValidator Unit Tests")
class ValidExtraScoresValidatorTest {

    private ValidExtraScoresValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new ValidExtraScoresValidator();
    }

    @Test
    @DisplayName("Happy Path: Null list should be valid")
    void isValid_NullList_ReturnsTrue() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    @DisplayName("Happy Path: Empty list should be valid")
    void isValid_EmptyList_ReturnsTrue() {
        assertTrue(validator.isValid(new ArrayList<>(), context));
    }

    @Test
    @DisplayName("Happy Path: List with valid scores (0.0 to 10.0) should be valid")
    void isValid_ValidScores_ReturnsTrue() {
        List<Double> scores = List.of(0.0, 5.5, 10.0, 8.25);
        assertTrue(validator.isValid(scores, context));
    }

    @Test
    @DisplayName("Happy Path: List with null elements should be valid (handled by other annotations if needed)")
    void isValid_ListWithNullElements_ReturnsTrue() {
        List<Double> scores = new ArrayList<>();
        scores.add(8.0);
        scores.add(null);
        assertTrue(validator.isValid(scores, context));
    }

    @Test
    @DisplayName("Exception Path: Score less than 0.0 should be invalid")
    void isValid_NegativeScore_ReturnsFalse() {
        List<Double> scores = List.of(8.0, -0.5, 9.0);
        assertFalse(validator.isValid(scores, context));
    }

    @Test
    @DisplayName("Exception Path: Score greater than 10.0 should be invalid")
    void isValid_ScoreGreaterThanTen_ReturnsFalse() {
        List<Double> scores = List.of(8.0, 10.1, 9.0);
        assertFalse(validator.isValid(scores, context));
    }
}
