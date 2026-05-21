package com.lqm.chat_service.it;

import com.lqm.chat_service.BaseIntegrationTest;
import com.lqm.chat_service.service.FirebaseTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Test cho ChatTokenController.
 * Endpoint: GET /api/secure/chat/token
 */
@DisplayName("ChatTokenFlowIT — Firebase Token REST Tests")
class ChatTokenFlowIT extends BaseIntegrationTest {

    @Autowired
    private FirebaseTokenService firebaseTokenService;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(firebaseTokenService);
    }

    @Test
    @DisplayName("Không có X-User-Email header → 403 (chưa xác thực)")
    void getFirebaseToken_NoAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/secure/chat/token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Có X-User-Email + X-User-Role header → 200 với customToken")
    void getFirebaseToken_ValidAuth_ShouldReturnToken() throws Exception {
        // Given
        when(firebaseTokenService.createCustomToken("user@school.edu"))
                .thenReturn("firebase-custom-token-xyz");

        // When & Then
        mockMvc.perform(get("/api/secure/chat/token")
                        .header("X-User-Email", "user@school.edu")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customToken").value("firebase-custom-token-xyz"));
    }

    @Test
    @DisplayName("Firebase throw exception → 500 với error message")
    void getFirebaseToken_FirebaseError_ShouldReturn500() throws Exception {
        // Given — mock ném FirebaseAuthException (checked) thông qua doAnswer
        Mockito.doAnswer(inv -> {
            throw new com.google.firebase.auth.FirebaseAuthException(
                    com.google.firebase.ErrorCode.INTERNAL,
                    "Firebase internal error", null, null, null);
        }).when(firebaseTokenService).createCustomToken(anyString());

        // When & Then — controller catches FirebaseAuthException và trả 500
        mockMvc.perform(get("/api/secure/chat/token")
                        .header("X-User-Email", "user@school.edu")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }
}
