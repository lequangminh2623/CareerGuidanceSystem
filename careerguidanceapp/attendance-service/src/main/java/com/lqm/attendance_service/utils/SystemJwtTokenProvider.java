package com.lqm.attendance_service.utils;

import com.lqm.attendance_service.configs.JwtConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Cung cấp JWT token "system" dùng cho các internal service-to-service call
 * trong MQTT thread (không có HTTP request context).
 *
 * Token được cache và tái sử dụng cho đến khi hết hạn.
 */
@Component
@Slf4j
public class SystemJwtTokenProvider {

    // Cache token để tránh generate lại liên tục
    private final AtomicReference<CachedToken> cachedToken = new AtomicReference<>();

    /**
     * Trả về Bearer JWT token hợp lệ cho internal service call.
     * Token được cache và tự động làm mới khi hết hạn.
     */
    public String getBearerToken() {
        CachedToken current = cachedToken.get();
        long now = System.currentTimeMillis();

        // Làm mới token nếu chưa có hoặc còn < 60s là hết hạn
        if (current == null || current.expiresAtMs() - now < 60_000L) {
            String token = generateSystemJwt();
            long expiresAtMs = now + JwtConfig.getExpirationMs();
            CachedToken fresh = new CachedToken(token, expiresAtMs);
            cachedToken.set(fresh);
            log.debug("Đã tạo system JWT token mới cho internal service call.");
            return "Bearer " + token;
        }

        return "Bearer " + current.token();
    }

    /**
     * Generate JWT HS256 với claim chuẩn.
     * Subject là "attendance-service" để phân biệt với user token.
     */
    private String generateSystemJwt() {
        try {
            long now = System.currentTimeMillis() / 1000;
            long exp = now + JwtConfig.getExpirationMs() / 1000;

            // Header
            String header = base64UrlEncode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");

            // Payload – role ADMIN để đủ quyền gọi /api/internal/secure/**
            String payloadJson = String.format(
                    "{\"sub\":\"attendance-service\",\"role\":\"ROLE_ADMIN\",\"iat\":%d,\"exp\":%d}",
                    now, exp);
            String payload = base64UrlEncode(payloadJson);

            // Signature
            String signingInput = header + "." + payload;
            String signature = sign(signingInput, JwtConfig.getSecret());

            return signingInput + "." + signature;
        } catch (Exception e) {
            log.error("Không thể tạo system JWT token: {}", e.getMessage());
            throw new RuntimeException("Failed to generate system JWT", e);
        }
    }

    private String base64UrlEncode(String input) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
    }

    private record CachedToken(String token, long expiresAtMs) {}
}
