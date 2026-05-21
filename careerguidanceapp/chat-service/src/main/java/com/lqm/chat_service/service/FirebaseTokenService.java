package com.lqm.chat_service.service;

import com.google.firebase.auth.FirebaseAuthException;

public interface FirebaseTokenService {

    /**
     * Generates a Firebase Custom Token for the given user email.
     * The email is used as uid in Firebase, ensuring consistency with user-service.
     *
     * @param userEmail the user's email (from X-User-Email header)
     * @return Firebase Custom Token string
     */
    String createCustomToken(String userEmail) throws FirebaseAuthException;
}
