package com.lqm.academic_service.configs;

import com.lqm.academic_service.utils.SystemJwtTokenProvider;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    
    private final SystemJwtTokenProvider systemJwtTokenProvider;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

            if (authorizationHeader != null) {
                requestTemplate.header(AUTHORIZATION_HEADER, authorizationHeader);
            }
        } else {
            // Không có HTTP context (ví dụ: RabbitMQ callback thread)
            // → dùng system JWT để xác thực internal service call
            requestTemplate.header(AUTHORIZATION_HEADER, systemJwtTokenProvider.getBearerToken());
        }
    }
}