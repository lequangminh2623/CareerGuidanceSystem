package com.lqm.chat_service.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.lqm.chat_service.service.FirebaseTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/secure/chat")
@RequiredArgsConstructor
public class ChatTokenController {

    private final FirebaseTokenService firebaseTokenService;

    /**
     * Generates a Firebase Custom Token for the authenticated user.
     * The user's email is extracted from the X-User-Email header (via AuthFilter).
     *
     * Frontend calls: GET /chat-service/api/secure/chat/token
     * Returns: { "customToken": "eyJ..." }
     */
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getFirebaseToken(Authentication authentication) {
        try {
            String email = authentication.getName(); // X-User-Email from AuthFilter
            String customToken = firebaseTokenService.createCustomToken(email);
            return ResponseEntity.ok(Map.of("customToken", customToken));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to generate Firebase token: " + e.getMessage()));
        }
    }
}
