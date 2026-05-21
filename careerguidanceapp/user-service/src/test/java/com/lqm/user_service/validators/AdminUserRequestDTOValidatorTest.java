package com.lqm.user_service.validators;

import com.lqm.user_service.dtos.AdminUserRequestDTO;
import com.lqm.user_service.models.Role;
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
 * Unit tests for {@link AdminUserRequestDTOValidator}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserRequestDTOValidator Unit Tests")
class AdminUserRequestDTOValidatorTest {

    @Mock private UserService userService;
    @Mock private StudentService studentService;

    @InjectMocks
    private AdminUserRequestDTOValidator validator;

    private Errors errorsFor(Object target) {
        return new BeanPropertyBindingResult(target, "adminUserRequestDTO");
    }

    @Nested
    @DisplayName("supports()")
    class Supports {

        @Test
        @DisplayName("Happy Path – trả về true cho AdminUserRequestDTO.class")
        void supports_correctClass_returnsTrue() {
            assertThat(validator.supports(AdminUserRequestDTO.class)).isTrue();
        }

        @Test
        @DisplayName("Exception Path – trả về false cho class không tương thích")
        void supports_wrongClass_returnsFalse() {
            assertThat(validator.supports(Object.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("validate() – Admin STUDENT role")
    class ValidateStudentRole {

        @Test
        @DisplayName("Happy Path – Admin tạo STUDENT hợp lệ, không có lỗi")
        void validate_validStudentDto_noErrors() {
            // Arrange
            AdminUserRequestDTO dto = AdminUserRequestDTO.builder()
                    .firstName("Minh")
                    .lastName("Le")
                    .email("minh@ou.edu.vn")
                    .role(Role.ROLE_STUDENT.getRoleName())   // "Student"
                    .code("2051012345")
                    .active(true)
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
        @DisplayName("Exception Path – Admin tạo STUDENT nhưng code null → lỗi 'user.student.code.notNull'")
        void validate_studentDtoNullCode_rejectsCode() {
            // Arrange
            AdminUserRequestDTO dto = AdminUserRequestDTO.builder()
                    .firstName("Minh")
                    .lastName("Le")
                    .email("minh@ou.edu.vn")
                    .role(Role.ROLE_STUDENT.getRoleName())
                    .code(null)
                    .active(true)
                    .gender(true)
                    .build();

            when(userService.existDuplicateUser("minh@ou.edu.vn", null)).thenReturn(false);

            Errors errors = errorsFor(dto);

            // Act
            validator.validate(dto, errors);

            // Assert
            assertThat(errors.hasFieldErrors("code")).isTrue();
            assertThat(errors.getFieldError("code").getCode()).isEqualTo("user.student.code.notNull");
        }

        @Test
        @DisplayName("Exception Path – code STUDENT trùng → lỗi 'user.student.code.unique'")
        void validate_duplicateStudentCode_rejectsCode() {
            // Arrange
            UUID id = UUID.randomUUID();
            AdminUserRequestDTO dto = AdminUserRequestDTO.builder()
                    .id(id)
                    .firstName("Minh")
                    .lastName("Le")
                    .email("minh@ou.edu.vn")
                    .role(Role.ROLE_STUDENT.getRoleName())
                    .code("2051012345")
                    .active(true)
                    .gender(true)
                    .build();

            when(userService.existDuplicateUser("minh@ou.edu.vn", id)).thenReturn(false);
            when(studentService.existDuplicateStudent("2051012345", id)).thenReturn(true);

            Errors errors = errorsFor(dto);

            // Act
            validator.validate(dto, errors);

            // Assert
            assertThat(errors.hasFieldErrors("code")).isTrue();
            assertThat(errors.getFieldError("code").getCode()).isEqualTo("user.student.code.unique");
        }
    }

    @Nested
    @DisplayName("validate() – Non-STUDENT roles")
    class ValidateNonStudentRole {

        @Test
        @DisplayName("Happy Path – Admin tạo TEACHER hợp lệ, không kiểm tra code")
        void validate_validTeacherDto_noCodeValidation() {
            // Arrange
            AdminUserRequestDTO dto = AdminUserRequestDTO.builder()
                    .firstName("Thay")
                    .lastName("Giao")
                    .email("teacher@ou.edu.vn")
                    .role(Role.ROLE_TEACHER.getRoleName())   // "Teacher"
                    .active(true)
                    .gender(false)
                    .build();

            when(userService.existDuplicateUser("teacher@ou.edu.vn", null)).thenReturn(false);

            Errors errors = errorsFor(dto);

            // Act
            validator.validate(dto, errors);

            // Assert
            assertThat(errors.hasErrors()).isFalse();
            // studentService không được gọi vì role không phải STUDENT
            verifyNoInteractions(studentService);
        }
    }

    @Nested
    @DisplayName("validate() – Common field errors")
    class ValidateCommonFields {

        @Test
        @DisplayName("Exception Path – email trùng → lỗi 'user.email.unique'")
        void validate_duplicateEmail_rejectsEmail() {
            // Arrange
            AdminUserRequestDTO dto = AdminUserRequestDTO.builder()
                    .firstName("Minh")
                    .lastName("Le")
                    .email("dup@ou.edu.vn")
                    .role("Admin")
                    .active(true)
                    .gender(true)
                    .build();

            when(userService.existDuplicateUser("dup@ou.edu.vn", null)).thenReturn(true);

            Errors errors = errorsFor(dto);

            // Act
            validator.validate(dto, errors);

            // Assert
            assertThat(errors.hasFieldErrors("email")).isTrue();
            assertThat(errors.getFieldError("email").getCode()).isEqualTo("user.email.unique");
        }

        @Test
        @DisplayName("Exception Path – role null → lỗi 'user.role.notNull'")
        void validate_nullRole_rejectsRole() {
            // Arrange
            AdminUserRequestDTO dto = AdminUserRequestDTO.builder()
                    .firstName("Minh")
                    .lastName("Le")
                    .email("minh@ou.edu.vn")
                    .role(null)
                    .active(true)
                    .gender(true)
                    .build();

            // email không lỗi → userService.existDuplicate sẽ được gọi
            when(userService.existDuplicateUser("minh@ou.edu.vn", null)).thenReturn(false);

            Errors errors = errorsFor(dto);

            // Act
            validator.validate(dto, errors);

            // Assert
            assertThat(errors.hasFieldErrors("role")).isTrue();
            assertThat(errors.getFieldError("role").getCode()).isEqualTo("user.role.notNull");
        }

        @Test
        @DisplayName("Exception Path – lastName trống → lỗi 'user.lastName.notNull'")
        void validate_blankLastName_rejectsLastName() {
            // Arrange
            AdminUserRequestDTO dto = AdminUserRequestDTO.builder()
                    .firstName("Minh")
                    .lastName("   ")
                    .email("minh@ou.edu.vn")
                    .role("Admin")
                    .active(true)
                    .gender(true)
                    .build();

            when(userService.existDuplicateUser("minh@ou.edu.vn", null)).thenReturn(false);
            Errors errors = errorsFor(dto);

            // Act
            validator.validate(dto, errors);

            // Assert
            assertThat(errors.hasFieldErrors("lastName")).isTrue();
            assertThat(errors.getFieldError("lastName").getCode()).isEqualTo("user.lastName.notNull");
        }
    }
}
