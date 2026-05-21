package com.lqm.user_service.config;

import com.lqm.user_service.exceptions.GlobalExceptionHandler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Test configuration để import {@link GlobalExceptionHandler} vào context của @WebMvcTest.
 *
 * Lý do cần file này:
 * - @WebMvcTest chỉ load tầng Web (Controller, Filter, ControllerAdvice).
 * - GlobalExceptionHandler inject HttpServletRequest → Spring tự động cung cấp trong
 *   servlet context nên không cần mock thêm.
 * - Mọi controller test class chỉ cần @Import(WebMvcTestConfig.class) để đảm bảo
 *   @RestControllerAdvice được đăng ký đúng.
 *
 * Lưu ý: Nếu @WebMvcTest đã scan được GlobalExceptionHandler tự động (trong cùng package),
 * thì không cần @Import này. Hãy thử trước khi dùng.
 */
@TestConfiguration
@Import(GlobalExceptionHandler.class)
public class WebMvcTestConfig {
    // Bean config chỉ để import GlobalExceptionHandler vào WebMvc test context
}
