package com.lqm.api_gateway.utils;

import com.lqm.api_gateway.configs.JwtConfig;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    public static Map<String, String> validateToken(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(JwtConfig.getSecret());

        if (!signedJWT.verify(verifier)) {
            return null;
        }

        Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expiration.before(new Date())) {
            return null;
        }

        String email = signedJWT.getJWTClaimsSet().getSubject();
        String role = signedJWT.getJWTClaimsSet().getStringClaim("roles");

        Map<String, String> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);
        return claims;
    }
}
