/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.lqm.models.User;
import com.lqm.services.UserService;
import com.lqm.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author Le Quang Minh
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class GoogleAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String CLIENT_ID;

    @Autowired
    private UserService userService;

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> body) {
        try {
            String token = body.get("token");

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance()
            ).setAudience(Collections.singletonList(CLIENT_ID)).build();

            GoogleIdToken idToken = verifier.verify(token);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String firstName = payload.get("given_name") != null ? (String) payload.get("given_name") : "";
                String lastName = payload.get("family_name") != null ? (String) payload.get("family_name") : "";

                String regex = "^[A-Za-z0-9._%+-]+@ou\\.edu\\.vn$";
                if (!email.matches(regex)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email phải có đuôi @ou.edu.vn.");
                }

                User user = userService.getUserByEmail(email).orElse(null);
                if (user == null) {
                    return ResponseEntity.ok(Map.of(
                            "email", email,
                            "firstName", firstName,
                            "lastName", lastName,
                            "isNewUser", true
                    ));
                }

                try {
                    String jwtToken = JwtUtils.generateToken(user.getEmail(), user.getRole());
                    return ResponseEntity.ok().body(Collections.singletonMap("token", jwtToken));
                } catch (Exception e) {
                    return ResponseEntity.status(500).body("Lỗi khi tạo JWT");
                }

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }
}
