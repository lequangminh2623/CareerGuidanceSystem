/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.utils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Le Quang Minh
 */
public class JwtUtils {

    private static final long EXPIRATION_MS = 86400000; // 1 ngày
    private static final String SECRET;

    static {
        Dotenv dotenv = Dotenv.load();
        SECRET = dotenv.get("SECRET");
    }

    public static String generateToken(String email, String role) throws Exception {
        JWSSigner signer = new MACSigner(SECRET);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(email)
                .claim("roles", role)
                .expirationTime(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claimsSet
        );

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public static Map<String, String> validateToken(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(SECRET);

        if (signedJWT.verify(verifier)) {
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration.after(new Date())) {
                String email = signedJWT.getJWTClaimsSet().getSubject();
                String role = signedJWT.getJWTClaimsSet().getStringClaim("roles");

                Map<String, String> result = new HashMap<>();
                result.put("email", email);
                result.put("role", role);
                return result;
            }
        }
        return null;
    }

}
