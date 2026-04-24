package com.lqm.attendance_service.exceptions;

import com.lqm.attendance_service.dtos.ExceptionResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import feign.FeignException;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j // Sử dụng logger thay vì printStackTrace cho môi trường Production
public class GlobalExceptionHandler {

    private final MessageSource messageSource;
    private final HttpServletRequest request; // Tiêm request để lấy URI tự động cho trường path

    // 1. Lỗi Validation (400) -> details là Map<Field, Message>
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDTO> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = messageSource.getMessage(error, LocaleContextHolder.getLocale());
            errors.put(fieldName, errorMessage);
        });

        return createResponse(HttpStatus.BAD_REQUEST, "Validation Failed", errors);
    }

    // 2. Lỗi User không tồn tại (400) -> details là Map chứa List ID
    @ExceptionHandler(NonExistingUsersException.class)
    public ResponseEntity<ExceptionResponseDTO> handleNonExistingUsersException(NonExistingUsersException ex) {
        Map<String, Object> details = Map.of("nonExistingUserIds", ex.getNonExistingIds());
        return createResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), details);
    }

    // 3. Các lỗi 400 chung
    @ExceptionHandler({IllegalArgumentException.class, BadRequestException.class})
    public ResponseEntity<ExceptionResponseDTO> handleBadRequest(RuntimeException ex) {
        return createResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // 4. Lỗi 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponseDTO> handleNotFound(ResourceNotFoundException ex) {
        return createResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // 5. Lỗi 401
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ExceptionResponseDTO> handleUnauthorized(UnauthorizedException ex) {
        return createResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
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

    // 7. Lỗi 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDTO> handleGeneralException(Exception ex) {
        // Ghi log chi tiết lỗi để admin có thể trace trong Log File hoặc Kibana
        log.error("Unhandled exception at {}: ", request.getRequestURI(), ex);
        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null);
    }

    // --- HELPER METHOD ---
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