package com.lqm.attendance_service.configs;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Interceptor xác thực JWT cho STOMP WebSocket CONNECT.
 * Client gửi token qua STOMP header: Authorization: Bearer <token>
 * Sử dụng nimbus-jose-jwt (có sẵn từ spring-boot-starter-security-oauth2-resource-server).
 */
@Component
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("WebSocket CONNECT bị từ chối: thiếu Authorization header");
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            try {
                SignedJWT signedJWT = SignedJWT.parse(token);
                JWSVerifier verifier = new MACVerifier(JwtConfig.getSecret());

                if (!signedJWT.verify(verifier)) {
                    throw new IllegalArgumentException("Invalid JWT signature");
                }

                Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
                if (expiration != null && expiration.before(new Date())) {
                    throw new IllegalArgumentException("JWT token expired");
                }

                String email = signedJWT.getJWTClaimsSet().getSubject();
                String role = signedJWT.getJWTClaimsSet().getStringClaim("role");

                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority(role));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                accessor.setUser(authentication);
                log.debug("WebSocket CONNECT xác thực thành công cho user: {}", email);

            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                log.warn("WebSocket CONNECT bị từ chối: JWT không hợp lệ - {}", e.getMessage());
                throw new IllegalArgumentException("Invalid JWT token");
            }
        }

        return message;
    }
}
