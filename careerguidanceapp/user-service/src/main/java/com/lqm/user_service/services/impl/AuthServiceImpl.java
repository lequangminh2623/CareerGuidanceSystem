package com.lqm.user_service.services.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.lqm.user_service.dtos.UserLoginDTO;
import com.lqm.user_service.exceptions.AuthenticationFailedException;
import com.lqm.user_service.exceptions.ResourceNotFoundException;
import com.lqm.user_service.models.User;
import com.lqm.user_service.repositories.UserRepository;
import com.lqm.user_service.services.AuthService;
import com.lqm.user_service.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepo;
    private final MessageSource messageSource;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String CLIENT_ID;

    public Map<String, Object> loginWithGoogle(String token) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance()
            ).setAudience(Collections.singletonList(CLIENT_ID)).build();

            GoogleIdToken idToken = verifier.verify(token);
            if (idToken == null) throw new IllegalArgumentException(messageSource.getMessage(
                    "token.invalid", null, Locale.getDefault())
            );

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");

            if (!email.matches("^[A-Za-z0-9._%+-]+@ou\\.edu\\.vn$")) {
                throw new IllegalArgumentException(messageSource.getMessage(
                        "user.email.invalid", null, Locale.getDefault())
                );
            }

            User user = userRepo.findByEmailAndActiveTrue(email).orElse(null);

            if (user == null) {
                return Map.of(
                        "email", email,
                        "firstName", firstName,
                        "lastName", lastName,
                        "isNewUser", true
                );
            }

            String jwtToken = JwtUtil.generateToken(user.getEmail(), user.getRole().name());
            return Map.of("token", jwtToken);

        } catch (Exception e) {
            throw new RuntimeException(messageSource.getMessage("error", null, Locale.getDefault()), e);
        }
    }

    public String login(UserLoginDTO userLoginDTO) throws Exception {
        User user = userRepo.findByEmailAndActiveTrue(userLoginDTO.email())
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage(
                        "user.notFound", null, Locale.getDefault()))
                );
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), userLoginDTO.password())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            throw new AuthenticationFailedException(
                    messageSource.getMessage("password.invalid", null, Locale.getDefault())
            );
        }

        return JwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }

}

