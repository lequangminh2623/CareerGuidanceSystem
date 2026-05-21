package com.lqm.score_service.services;

import com.lqm.score_service.clients.SectionClient;
import com.lqm.score_service.clients.UserClient;
import com.lqm.score_service.dtos.UserResponseDTO;
import com.lqm.score_service.services.impl.SecurityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityServiceImpl Unit Tests")
class SecurityServiceImplTest {

    @Mock
    private SectionClient sectionClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private SecurityServiceImpl securityService;

    private UUID targetId;
    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        targetId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("hasPermission()")
    class HasPermissionTests {

        @Test
        @DisplayName("Happy Path: User is authenticated and has permission -> Returns true")
        void hasPermission_UserAuthenticatedAndHasPermission_ReturnsTrue() {
            // Arrange
            UserResponseDTO currentUser = new UserResponseDTO(currentUserId, "code", "firstName", "lastName");
            given(userClient.getCurrentUser()).willReturn(currentUser);
            given(sectionClient.checkTeacherPermission(targetId, currentUserId)).willReturn(true);

            // Act
            boolean result = securityService.hasPermission(targetId, "SECTION", "MANAGE_SCORES");

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Exception Path: User is not authenticated -> Returns false")
        void hasPermission_UserNotAuthenticated_ReturnsFalse() {
            // Arrange
            given(userClient.getCurrentUser()).willReturn(null);

            // Act
            boolean result = securityService.hasPermission(targetId, "SECTION", "MANAGE_SCORES");

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Exception Path: Target type is unknown -> Returns false")
        void hasPermission_UnknownTargetType_ReturnsFalse() {
            // Arrange
            UserResponseDTO currentUser = new UserResponseDTO(currentUserId, "code", "firstName", "lastName");
            given(userClient.getCurrentUser()).willReturn(currentUser);

            // Act
            boolean result = securityService.hasPermission(targetId, "UNKNOWN", "MANAGE_SCORES");

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Exception Path: Permission is unknown -> Returns false")
        void hasPermission_UnknownPermission_ReturnsFalse() {
            // Arrange
            UserResponseDTO currentUser = new UserResponseDTO(currentUserId, "code", "firstName", "lastName");
            given(userClient.getCurrentUser()).willReturn(currentUser);

            // Act
            boolean result = securityService.hasPermission(targetId, "SECTION", "UNKNOWN");

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Exception Path: Null parameters -> Returns false")
        void hasPermission_NullParameters_ReturnsFalse() {
            // Act & Assert
            assertThat(securityService.hasPermission(null, "SECTION", "MANAGE_SCORES")).isFalse();
            assertThat(securityService.hasPermission(targetId, null, "MANAGE_SCORES")).isFalse();
            assertThat(securityService.hasPermission(targetId, "SECTION", null)).isFalse();
        }

        @Test
        @DisplayName("Exception Path: Feign client throws exception -> Returns false gracefully")
        void hasPermission_FeignClientException_ReturnsFalse() {
            // Arrange
            given(userClient.getCurrentUser()).willThrow(new RuntimeException("Feign error"));

            // Act
            boolean result = securityService.hasPermission(targetId, "SECTION", "MANAGE_SCORES");

            // Assert
            assertThat(result).isFalse();
        }
    }
}
