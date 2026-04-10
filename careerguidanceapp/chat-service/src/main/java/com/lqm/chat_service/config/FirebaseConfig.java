package com.lqm.chat_service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account.base64}")
    private String serviceAccountBase64;

    @PostConstruct
    public void initFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = getServiceAccountStream();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized successfully.");
        }
    }

    /**
     * Supports two ways to provide the service account:
     * 1. Base64-encoded JSON string (recommended for Docker/K8s via env var)
     * 2. File path to the JSON file (for local development)
     */
    private InputStream getServiceAccountStream() throws IOException {
        if (serviceAccountBase64 != null && !serviceAccountBase64.isBlank()) {
            log.info("Loading Firebase credentials from Base64 env variable.");
            byte[] decoded = Base64.getDecoder().decode(serviceAccountBase64);
            return new ByteArrayInputStream(decoded);
        }

        throw new IllegalStateException(
                "Firebase Service Account not configured. " +
                "Set FIREBASE_SERVICE_ACCOUNT_BASE64 or FIREBASE_SERVICE_ACCOUNT_PATH environment variable."
        );
    }
}
