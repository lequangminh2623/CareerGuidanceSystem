package com.lqm.user_service.services.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.lqm.user_service.dtos.UserLoginDTO;
import com.lqm.user_service.exceptions.AuthenticationFailedException;
import com.lqm.user_service.exceptions.ResourceNotFoundException;
import com.lqm.user_service.models.User;
import com.lqm.user_service.repositories.UserRepository;
import com.lqm.user_service.services.AuthService;
import com.lqm.user_service.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;
import java.util.Map;

/**
 * Refactor so với phiên bản cũ:
 * 1. {@link GoogleIdTokenVerifier} được inject qua constructor (không còn tạo trực tiếp
 *    trong method) → có thể mock hoàn toàn trong unit test.
 * 2. {@link JwtUtil} được inject qua constructor (không còn gọi static method) → testable.
 * 3. try-catch được tách rõ: chỉ bắt {@link GeneralSecurityException} và {@link IOException}
 *    từ bước I/O (verifier.verify), không bắt các exception nghiệp vụ như
 *    {@link IllegalArgumentException} → tránh nuốt lỗi validation.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepo;
    private final MessageSource messageSource;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final JwtUtil jwtUtil;

    // -------------------------------------------------------------------------
    public Map<String, Object> loginWithGoogle(String token) {
        // 1. Xác minh token với Google – chỉ bắt lỗi I/O tại đây
        GoogleIdToken idToken;
        try {
            idToken = googleIdTokenVerifier.verify(token);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(
                    messageSource.getMessage("error", null, Locale.getDefault()), e);
        }

        // 2. Validate kết quả – exception nghiệp vụ, không bị nuốt
        if (idToken == null) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("token.invalid", null, Locale.getDefault()));
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email     = payload.getEmail();
        String firstName = (String) payload.get("given_name");
        String lastName  = (String) payload.get("family_name");

        // 3. Validate định dạng email
        if (!email.matches("^[A-Za-z0-9._%+-]+@ou\\.edu\\.vn$")) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("user.email.invalid", null, Locale.getDefault()));
        }

        // 4. Tra cứu User
        User user = userRepo.findByEmailAndActiveTrue(email).orElse(null);
        if (user == null) {
            return Map.of(
                    "email", email,
                    "firstName", firstName,
                    "lastName", lastName,
                    "isNewUser", true
            );
        }

        // 5. Tạo JWT
        try {
            String jwtToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
            return Map.of("token", jwtToken);
        } catch (Exception e) {
            throw new RuntimeException(
                    messageSource.getMessage("error", null, Locale.getDefault()), e);
        }
    }

    // -------------------------------------------------------------------------
    public String login(UserLoginDTO userLoginDTO) throws Exception {
        User user = userRepo.findByEmailAndActiveTrue(userLoginDTO.email())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("user.notFound", null, Locale.getDefault())));

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), userLoginDTO.password())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            throw new AuthenticationFailedException(
                    messageSource.getMessage("password.invalid", null, Locale.getDefault()));
        }

        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }
}
