package com.lqm.admin_service.exceptions;

import com.lqm.admin_service.dtos.ExceptionResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import feign.FeignException;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j // Khuyến khích dùng để log lỗi thay vì printStackTrace
public class GlobalExceptionHandler {

    @Autowired
    private HttpServletRequest request; // Tiêm trực tiếp vào để dùng chung

    // 2. Xử lý NonExistingUsersException (400)
    @ExceptionHandler(NonExistingUsersException.class)
    public ResponseEntity<ExceptionResponseDTO> handleNonExistingUsersException(NonExistingUsersException ex) {
        Map<String, Object> details = Map.of("nonExistingUserIds", ex.getNonExistingIds());
        return createResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), details);
    }

    // 3. Nhóm các lỗi 400 Bad Request chung
    @ExceptionHandler({IllegalArgumentException.class, BadRequestException.class})
    public ResponseEntity<ExceptionResponseDTO> handleBadRequest(RuntimeException ex) {
        return createResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // 4. Lỗi 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponseDTO> handleNotFound(ResourceNotFoundException ex) {
        return createResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // 5. Lỗi 401 Unauthorized
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ExceptionResponseDTO> handleUnauthorized(UnauthorizedException ex) {
        return createResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    // 6. Lỗi 403 Forbidden
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ExceptionResponseDTO> handleForbidden(ForbiddenException ex) {
        return createResponse(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    @ExceptionHandler(NoFallbackAvailableException.class)
    public ResponseEntity<ExceptionResponseDTO> handleNoFallbackAvailableException(NoFallbackAvailableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof FeignException feignEx) {
            return handleFeignException(feignEx);
        }
        log.error("Circuit breaker opened without fallback at {}: ", request.getRequestURI(), ex);
        return createResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service is currently unavailable. No fallback available.", null);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ExceptionResponseDTO> handleFeignException(FeignException ex) {
        log.error("Feign client error at {}: {}", request.getRequestURI(), ex.getMessage());
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return createResponse(status, ex.getMessage(), null);
    }

    // 7. Lỗi 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDTO> handleGeneralException(Exception ex) {
        log.error("Unhandled exception occurred at {}: ", request.getRequestURI(), ex);
        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null);
    }

    // --- HELPER METHOD ---
    // Đã bỏ tham số request ở cuối vì đã được @Autowired ở trên
    private ResponseEntity<ExceptionResponseDTO> createResponse(HttpStatus status, String message, Object details) {
        ExceptionResponseDTO response = new ExceptionResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                details,
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, status);
    }
}