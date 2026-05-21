package com.lqm.score_service.validators;

import com.lqm.score_service.clients.UserClient;
import com.lqm.score_service.dtos.ScoreRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScoreRequestDTOValidator Unit Tests")
class ScoreRequestDTOValidatorTest {

    @Mock
    private UserClient userClient;

    @InjectMocks
    private ScoreRequestDTOValidator validator;

    private UUID studentId;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
    }

    @Test
    @DisplayName("supports: should support ScoreRequestDTO class")
    void supports() {
        assertTrue(validator.supports(ScoreRequestDTO.class));
        assertFalse(validator.supports(Object.class));
    }

    @Nested
    @DisplayName("validate()")
    class ValidateTests {

        @Test
        @DisplayName("Happy Path: student exists -> no errors added")
        void validate_StudentExists_NoErrors() {
            // Arrange
            ScoreRequestDTO dto = new ScoreRequestDTO();
            dto.setStudentId(studentId);
            Errors errors = new BeanPropertyBindingResult(dto, "scoreRequestDTO");

            given(userClient.checkStudentExistById(studentId)).willReturn(true);

            // Act
            validator.validate(dto, errors);

            // Assert
            assertFalse(errors.hasErrors());
        }

        @Test
        @DisplayName("Exception Path: student does not exist -> adds student.notFound error")
        void validate_StudentDoesNotExist_AddsError() {
            // Arrange
            ScoreRequestDTO dto = new ScoreRequestDTO();
            dto.setStudentId(studentId);
            Errors errors = new BeanPropertyBindingResult(dto, "scoreRequestDTO");

            given(userClient.checkStudentExistById(studentId)).willReturn(false);

            // Act
            validator.validate(dto, errors);

            // Assert
            assertTrue(errors.hasFieldErrors("studentId"));
            assertThat(errors.getFieldError("studentId").getCode()).isEqualTo("student.notFound");
        }
    }
}
