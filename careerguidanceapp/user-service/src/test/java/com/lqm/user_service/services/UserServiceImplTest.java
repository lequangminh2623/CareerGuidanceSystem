package com.lqm.user_service.services;

import com.lqm.user_service.exceptions.ResourceNotFoundException;
import com.lqm.user_service.models.Role;
import com.lqm.user_service.models.User;
import com.lqm.user_service.repositories.StudentRepository;
import com.lqm.user_service.repositories.UserRepository;
import com.lqm.user_service.services.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private StudentRepository studentRepo;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private UserServiceImpl userService;

    // ------------------------------------------------------------------ helpers
    private User buildUser(UUID id, Role role) {
        return User.builder()
                .id(id)
                .firstName("Minh")
                .lastName("Le")
                .email("test@ou.edu.vn")
                .password("encoded_password")
                .role(role)
                .gender(true)
                .active(true)
                .build();
    }

    private void stubMessage(String code, String msg) {
        when(messageSource.getMessage(eq(code), any(), any())).thenReturn(msg);
    }

    // ================================================================== TESTS
    @Nested
    @DisplayName("getUserById()")
    class GetUserById {

        @Test
        @DisplayName("Happy Path – trả về User khi tìm thấy theo ID")
        void getUserById_found_returnsUser() {
            // Arrange
            UUID id = UUID.randomUUID();
            User expected = buildUser(id, Role.ROLE_STUDENT);
            when(userRepo.findById(id)).thenReturn(Optional.of(expected));

            // Act
            User actual = userService.getUserById(id);

            // Assert
            assertThat(actual).isEqualTo(expected);
            verify(userRepo).findById(id);
        }

        @Test
        @DisplayName("Exception Path – ném ResourceNotFoundException khi không tìm thấy User")
        void getUserById_notFound_throwsResourceNotFoundException() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(userRepo.findById(id)).thenReturn(Optional.empty());
            stubMessage("user.notFound", "User not found");

            // Act & Assert
            assertThatThrownBy(() -> userService.getUserById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("loadUserByUsername()")
    class LoadUserByUsername {

        @Test
        @DisplayName("Happy Path – trả về UserDetails đúng khi email tồn tại")
        void loadUserByUsername_existingEmail_returnsUserDetails() {
            // Arrange
            UUID id = UUID.randomUUID();
            User user = buildUser(id, Role.ROLE_STUDENT);
            when(userRepo.findByEmailAndActiveTrue("test@ou.edu.vn")).thenReturn(Optional.of(user));

            // Act
            UserDetails details = userService.loadUserByUsername("test@ou.edu.vn");

            // Assert
            assertThat(details.getUsername()).isEqualTo("test@ou.edu.vn");
            assertThat(details.getPassword()).isEqualTo("encoded_password");
        }

        @Test
        @DisplayName("Exception Path – ném UsernameNotFoundException khi email không tồn tại")
        void loadUserByUsername_unknownEmail_throwsUsernameNotFoundException() {
            // Arrange
            when(userRepo.findByEmailAndActiveTrue("ghost@ou.edu.vn")).thenReturn(Optional.empty());
            stubMessage("user.email.invalid", "Email invalid");

            // Act & Assert
            assertThatThrownBy(() -> userService.loadUserByUsername("ghost@ou.edu.vn"))
                    .isInstanceOf(UsernameNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("saveUser()")
    class SaveUser {

        @Test
        @DisplayName("Happy Path – tạo User STUDENT mới: encode password, gán role, tạo Student")
        void saveUser_newStudent_encodesPasswordAndCreatesStudentRecord() {
            // Arrange
            User user = User.builder()
                    .firstName("Minh")
                    .lastName("Le")
                    .email("minh@ou.edu.vn")
                    .password("rawPass")
                    .gender(true)
                    .active(true)
                    .build(); // role = null → will default to ROLE_STUDENT

            when(passwordEncoder.encode("rawPass")).thenReturn("encoded_rawPass");
            when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            User saved = userService.saveUser(user, null, "2051012345");

            // Assert
            assertThat(saved.getPassword()).isEqualTo("encoded_rawPass");
            assertThat(saved.getRole()).isEqualTo(Role.ROLE_STUDENT);
            assertThat(saved.getActive()).isTrue();
            assertThat(saved.getStudent()).isNotNull();
            assertThat(saved.getStudent().getCode()).isEqualTo("2051012345");
            verify(passwordEncoder).encode("rawPass");
            verify(userRepo).save(user);
        }

        @Test
        @DisplayName("Happy Path – cập nhật User với ảnh mới: upload Cloudinary, cập nhật avatar URL")
        void saveUser_withFile_uploadsAvatarToCloudinary() {
            // Arrange
            UUID id = UUID.randomUUID();
            User user = buildUser(id, Role.ROLE_TEACHER);
            user.setAvatar("old_url");

            MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpg",
                    "image/jpeg", new byte[] { 1, 2, 3 });

            when(passwordEncoder.encode(any())).thenReturn("encoded");
            when(cloudinaryService.uploadFile(file)).thenReturn("new_cloudinary_url");
            when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            User saved = userService.saveUser(user, file, null);

            // Assert
            assertThat(saved.getAvatar()).isEqualTo("new_cloudinary_url");
            verify(cloudinaryService).deleteFile("old_url");
            verify(cloudinaryService).uploadFile(file);
        }

        @Test
        @DisplayName("Happy Path – avatar null/rỗng/undefined → gán URL mặc định")
        void saveUser_nullAvatar_setsDefaultAvatar() {
            // Arrange
            User user = buildUser(null, Role.ROLE_TEACHER);
            user.setAvatar(null);

            when(passwordEncoder.encode(any())).thenReturn("encoded");
            when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            User saved = userService.saveUser(user, null, null);

            // Assert
            assertThat(saved.getAvatar())
                    .isNotNull()
                    .startsWith("https://res.cloudinary.com");
        }

        @Test
        @DisplayName("Happy Path – password null → tự động encode password mặc định '1'")
        void saveUser_nullPassword_encodesDefaultPassword() {
            // Arrange
            User user = buildUser(null, Role.ROLE_ADMIN);
            user.setPassword(null);

            when(passwordEncoder.encode("1")).thenReturn("encoded_default");
            when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            User saved = userService.saveUser(user, null, null);

            // Assert
            assertThat(saved.getPassword()).isEqualTo("encoded_default");
            verify(passwordEncoder).encode("1");
        }
    }

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUser {

        @Test
        @DisplayName("Happy Path – xóa User và file avatar trên Cloudinary")
        void deleteUser_existingUser_deletesAvatarAndUser() {
            // Arrange
            UUID id = UUID.randomUUID();
            User user = buildUser(id, Role.ROLE_STUDENT);
            user.setAvatar("avatar_url");
            when(userRepo.findById(id)).thenReturn(Optional.of(user));

            // Act
            userService.deleteUser(id);

            // Assert
            verify(cloudinaryService).deleteFile("avatar_url");
            verify(userRepo).deleteById(id);
        }

        @Test
        @DisplayName("Exception Path – ném ResourceNotFoundException khi User không tồn tại")
        void deleteUser_nonExistentUser_throwsResourceNotFoundException() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(userRepo.findById(id)).thenReturn(Optional.empty());
            stubMessage("user.notFound", "User not found");

            // Act & Assert
            assertThatThrownBy(() -> userService.deleteUser(id))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(cloudinaryService, never()).deleteFile(any());
            verify(userRepo, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("getCurrentUser()")
    class GetCurrentUser {

        private SecurityContext mockSecurityContext;
        private Authentication mockAuthentication;

        @BeforeEach
        void setupSecurityContext() {
            mockSecurityContext = mock(SecurityContext.class);
            mockAuthentication = mock(Authentication.class);
            SecurityContextHolder.setContext(mockSecurityContext);
        }

        @AfterEach
        void clearSecurityContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("Happy Path – trả về User hiện tại từ SecurityContext")
        void getCurrentUser_authenticated_returnsUser() {
            // Arrange
            UUID id = UUID.randomUUID();
            User expected = buildUser(id, Role.ROLE_STUDENT);
            when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
            when(mockAuthentication.getPrincipal()).thenReturn("test@ou.edu.vn");
            when(userRepo.findByEmailAndActiveTrue("test@ou.edu.vn")).thenReturn(Optional.of(expected));

            // Act
            User actual = userService.getCurrentUser();

            // Assert
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("Exception Path – ném NullPointerException khi chưa đăng nhập (authentication null)")
        void getCurrentUser_noAuthentication_throwsNullPointerException() {
            // Arrange – SecurityContext trả về null authentication
            when(mockSecurityContext.getAuthentication()).thenReturn(null);

            // Act & Assert
            // Objects.requireNonNull() ném NPE khi authentication == null
            assertThatThrownBy(() -> userService.getCurrentUser())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Exception Path – ném ResourceNotFoundException khi email trong token không tìm thấy User")
        void getCurrentUser_emailNotInDb_throwsResourceNotFoundException() {
            // Arrange
            when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
            when(mockAuthentication.getPrincipal()).thenReturn("deleted@ou.edu.vn");
            when(userRepo.findByEmailAndActiveTrue("deleted@ou.edu.vn")).thenReturn(Optional.empty());
            stubMessage("user.notFound", "User not found");

            // Act & Assert
            assertThatThrownBy(() -> userService.getCurrentUser())
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("existDuplicateUser()")
    class ExistDuplicateUser {

        @Test
        @DisplayName("Happy Path – trả về true khi email đã tồn tại")
        void existDuplicateUser_existingEmail_returnsTrue() {
            // Arrange
            when(userRepo.existsByEmailAndExcludeId("dup@ou.edu.vn", null)).thenReturn(true);

            // Act
            boolean result = userService.existDuplicateUser("dup@ou.edu.vn", null);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Happy Path – trả về false khi email chưa tồn tại")
        void existDuplicateUser_freshEmail_returnsFalse() {
            // Arrange
            UUID excludeId = UUID.randomUUID();
            when(userRepo.existsByEmailAndExcludeId("new@ou.edu.vn", excludeId)).thenReturn(false);

            // Act
            boolean result = userService.existDuplicateUser("new@ou.edu.vn", excludeId);

            // Assert
            assertThat(result).isFalse();
        }
    }
}
