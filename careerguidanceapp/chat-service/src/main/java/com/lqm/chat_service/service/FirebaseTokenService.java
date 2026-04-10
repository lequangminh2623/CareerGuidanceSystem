package com.lqm.chat_service.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FirebaseTokenService {

    /**
     * Generates a Firebase Custom Token for the given user email.
     * The email is used as uid in Firebase, ensuring consistency with user-service.
     *
     * @param userEmail the user's email (from X-User-Email header)
     * @return Firebase Custom Token string
     */
    public String createCustomToken(String userEmail) throws FirebaseAuthException {
        // Firebase UID allows alphanumeric, dots, dashes, underscores, and @.
        // Using email directly as UID ensures consistency across systems.
        String customToken = FirebaseAuth.getInstance().createCustomToken(userEmail);
        log.info("Generated Firebase Custom Token for user: {}", userEmail);
        return customToken;
    }
}
