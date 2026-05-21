package com.lqm.chat_service.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseTokenServiceImplTest {

    @InjectMocks
    private FirebaseTokenServiceImpl firebaseTokenService;

    @Test
    @DisplayName("Happy Path: returns token successfully")
    void createCustomToken_ReturnsToken() throws Exception {
        String email = "teacher@test.com";
        String expectedToken = "mock-firebase-token";

        FirebaseAuth firebaseAuthMock = mock(FirebaseAuth.class);
        when(firebaseAuthMock.createCustomToken(email)).thenReturn(expectedToken);

        try (MockedStatic<FirebaseAuth> mockedStatic = mockStatic(FirebaseAuth.class)) {
            mockedStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuthMock);

            String token = firebaseTokenService.createCustomToken(email);

            assertThat(token).isEqualTo(expectedToken);
            verify(firebaseAuthMock).createCustomToken(email);
        }
    }

    @Test
    @DisplayName("Exception Path: throws FirebaseAuthException")
    void createCustomToken_ThrowsException() throws Exception {
        String email = "teacher@test.com";

        FirebaseAuth firebaseAuthMock = mock(FirebaseAuth.class);
        FirebaseAuthException authException = mock(FirebaseAuthException.class);
        when(authException.getMessage()).thenReturn("Error");
        when(firebaseAuthMock.createCustomToken(email)).thenThrow(authException);

        try (MockedStatic<FirebaseAuth> mockedStatic = mockStatic(FirebaseAuth.class)) {
            mockedStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuthMock);

            assertThatThrownBy(() -> firebaseTokenService.createCustomToken(email))
                    .isInstanceOf(FirebaseAuthException.class)
                    .hasMessageContaining("Error");
        }
    }
}
