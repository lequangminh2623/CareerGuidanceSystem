package com.lqm.user_service.utils;

import com.lqm.user_service.configs.JwtConfig;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;

public class JwtUtil {

    public static String generateToken(String email, String role) throws Exception {
        JWSSigner signer = new MACSigner(JwtConfig.getSecret());

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(email)
                .claim("roles", role)
                .expirationTime(new Date(System.currentTimeMillis() + JwtConfig.getExpirationMs()))
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claimsSet
        );

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

}
