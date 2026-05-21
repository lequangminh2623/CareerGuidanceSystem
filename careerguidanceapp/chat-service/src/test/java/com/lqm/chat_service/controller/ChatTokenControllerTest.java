package com.lqm.chat_service.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.lqm.chat_service.service.FirebaseTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatTokenController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatTokenControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private FirebaseTokenService firebaseTokenService;

        private static final String BASE_URL = "/api/secure/chat/token";

        @Test
        @DisplayName("Happy Path: returns 200 with customToken")
        void getFirebaseToken_HappyPath_Returns200() throws Exception {
                given(firebaseTokenService.createCustomToken("teacher@test.com")).willReturn("mock-token-123");

                org.springframework.security.authentication.UsernamePasswordAuthenticationToken principal = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                "teacher@test.com", null);

                mockMvc.perform(get(BASE_URL).principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.customToken").value("mock-token-123"));
        }

        @Test
        @DisplayName("Exception Path: returns 500 when Firebase fails")
        void getFirebaseToken_FirebaseException_Returns500() throws Exception {
                FirebaseAuthException exception = mock(FirebaseAuthException.class);
                when(exception.getMessage()).thenReturn("Firebase error");
                given(firebaseTokenService.createCustomToken("teacher@test.com")).willThrow(exception);

                org.springframework.security.authentication.UsernamePasswordAuthenticationToken principal = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                "teacher@test.com", null);

                mockMvc.perform(get(BASE_URL).principal(principal))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.error")
                                                .value("Failed to generate Firebase token: Firebase error"));
        }
}
