package com.lqm.attendance_service.configs;

import com.lqm.attendance_service.utils.SystemJwtTokenProvider;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign interceptor tự động đính kèm JWT token vào mọi request.
 *
 * - Nếu đang trong HTTP request context (user-facing API): forward JWT của người dùng.
 * - Nếu không có HTTP context (MQTT thread, async task): dùng system JWT để
 *   xác thực service-to-service call nội bộ.
 */
@Component
@RequiredArgsConstructor
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final SystemJwtTokenProvider systemJwtTokenProvider;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            // Đang trong HTTP request context → forward JWT của người dùng
            HttpServletRequest request = attributes.getRequest();
            String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
            if (authorizationHeader != null) {
                requestTemplate.header(AUTHORIZATION_HEADER, authorizationHeader);
            }
        } else {
            // Không có HTTP context (ví dụ: MQTT callback thread)
            // → dùng system JWT để xác thực internal service call
            requestTemplate.header(AUTHORIZATION_HEADER, systemJwtTokenProvider.getBearerToken());
        }
    }
}