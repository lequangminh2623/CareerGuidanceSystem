package com.lqm.user_service.validators;

import com.lqm.user_service.dtos.UserRequestDTO;
import com.lqm.user_service.services.StudentService;
import com.lqm.user_service.services.UserService;
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
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserRequestDTOValidator}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRequestDTOValidator Unit Tests")
class UserRequestDTOValidatorTest {

    @Mock private UserService userService;
    @Mock private StudentService studentService;

    @InjectMocks
    private UserRequestDTOValidator validator;

    private Errors errorsFor(Object target) {
        return new BeanPropertyBindingResult(target, "userRequestDTO");
    }

    @Nested
    @DisplayName("supports()")
    class Supports {

        @Test
        @DisplayName("Happy Path – trả về true cho UserRequestDTO.class")
        void supports_correctClass_returnsTrue() {
            assertThat(validator.supports(UserRequestDTO.class)).isTrue();
        }

        @Test
        @DisplayName("Exception Path – trả về false cho class khác")
        void supports_wrongClass_returnsFalse() {
            assertThat(validator.supports(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("validate()")
    class Validate {

        @Test
        @DisplayName("Happy Path – DTO hợp lệ không có lỗi nào")
        void validate_validDto_noErrors() {
            // Arrange
            UserRequestDTO dto = UserRequestDTO.builder()
                    .firstName("Minh")
                    .lastName("Le")
                    .email("minh@ou.edu.vn")
                    .password("securePass123")
                    .code("2051012345")
                    .gender(true)
                    .build();

            when(userService.existDuplicateUser("minh@ou.edu.vn", null)).thenReturn(false);
            when(studentService.existDuplicateStudent("2051012345", null)).thenReturn(false);

            Errors errors = errorsFor(dto);

            // Act
            validator.validate(dto, errors);

            // Assert
            assertThat(errors.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("Exception Path – firstName null → lỗi 'user.firstName.notNull'")
        void validate_nullFirstName_rejectsFirstName() {
            // Arrange
            UserRequestDTO dto = UserRequestDTO.builder()
                    .firstName(null)
                    .lastName("Le")
                    .email("minh@ou.edu.vn")
                    .password("pass")
                    .code("2051012345")
                    .gender(true)
                    .build();

            when(userService.existDuplicateUser(any(), any())).thenReturn(false);
            when(studentService.existDuplicateStudent(any(), any())).thenReturn(false);

            Errors errors = errorsFor(dto);

            // Act
            validator.validate(dto, errors);

            // Assert
            assertThat(errors.hasFieldErrors("firstName")).isTrue();
            assertThat(errors.getFieldError("firstName").getCode()).isEqualTo("user.firstName.notNull");
        }

        @Test
        @DisplayName("Exception Path – email đã tồn tại → lỗi 'user.email.unique'")
        void validate_duplicateEmail_rejectsEmail() {
            // Arrange
            UUID id = UUID.randomUUID();
            UserRequestDTO dto = UserRequestDTO.builder()
                    .id(id)
                    .firstName("Minh")
                    .lastName("Le")
                    .email("dup@ou.edu.vn")
                    .password("pass")
                    .code("2051012345")
                    .gender(true)
                    .build();

            when(userService.existDuplicateUser("dup@ou.edu.vn", id)).thenReturn(true);

            Errors errors = errorsFor(dto);

            // Act
            validator.validate(dto, errors);

            // Assert
            assertThat(errors.hasFieldErrors("email")).isTrue();
            assertThat(errors.getFieldError("email").getCode()).isEqualTo("user.email.unique");
        }

        @Test
        @DisplayName("Exception Path – mã sinh viên đã tồn tại → lỗi 'user.student.code.unique'")
        void validate_duplicateStudentCode_rejectsCode() {
            // Arrange
            UserRequestDTO dto = UserRequestDTO.builder()
                    .firstName("Minh")
                    .lastName("Le")
                    .email("new@ou.edu.vn")
                    .password("pass")
                    .code("2051012345")
                    .gender(true)
                    .build();

            when(userService.existDuplicateUser("new@ou.edu.vn", null)).thenReturn(false);
            when(studentService.existDuplicateStudent("2051012345", null)).thenReturn(true);

            Errors errors = errorsFor(dto);

            // Act
            validator.validate(dto, errors);

            // Assert
            assertThat(errors.hasFieldErrors("code")).isTrue();
            assertThat(errors.getFieldError("code").getCode()).isEqualTo("user.student.code.unique");
        }

        @Test
        @DisplayName("Exception Path – code null → lỗi 'user.student.code.notNull'")
        void validate_nullCode_rejectsCode() {
            // Arrange
            UserRequestDTO dto = UserRequestDTO.builder()
                    .firstName("Minh")
                    .lastName("Le")
                    .email("new@ou.edu.vn")
                    .password("pass")
                    .code(null)
                    .gender(true)
                    .build();

            when(userService.existDuplicateUser("new@ou.edu.vn", null)).thenReturn(false);

            Errors errors = errorsFor(dto);

            // Act
            validator.validate(dto, errors);

            // Assert
            assertThat(errors.hasFieldErrors("code")).isTrue();
            assertThat(errors.getFieldError("code").getCode()).isEqualTo("user.student.code.notNull");
        }

        @Test
        @DisplayName("Exception Path – password null → lỗi 'user.password.notNull'")
        void validate_nullPassword_rejectsPassword() {
            // Arrange
            UserRequestDTO dto = UserRequestDTO.builder()
                    .firstName("Minh")
                    .lastName("Le")
                    .email("new@ou.edu.vn")
                    .password(null)
                    .code("2051012345")
                    .gender(true)
                    .build();

            when(userService.existDuplicateUser("new@ou.edu.vn", null)).thenReturn(false);
            when(studentService.existDuplicateStudent("2051012345", null)).thenReturn(false);

            Errors errors = errorsFor(dto);

            // Act
            validator.validate(dto, errors);

            // Assert
            assertThat(errors.hasFieldErrors("password")).isTrue();
        }
    }
}
