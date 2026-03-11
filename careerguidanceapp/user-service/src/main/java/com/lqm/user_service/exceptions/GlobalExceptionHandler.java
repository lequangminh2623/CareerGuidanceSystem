package com.lqm.user_service.exceptions;

import com.lqm.user_service.dtos.ExceptionResponseDTO;
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
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j // Sử dụng logger thay vì printStackTrace
public class GlobalExceptionHandler {

    private final MessageSource messageSource;
    private final HttpServletRequest request; // Tiêm request để lấy path tự động

    // 1. Xử lý lỗi Validation (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDTO> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = messageSource.getMessage(error, LocaleContextHolder.getLocale());
            errors.put(fieldName, errorMessage);
        });

        return createResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    // 2. Xử lý User không tồn tại (400)
    @ExceptionHandler(NonExistingUsersException.class)
    public ResponseEntity<ExceptionResponseDTO> handleNonExistingUsersException(NonExistingUsersException ex) {
        Map<String, Object> details = Map.of("nonExistingUserIds", ex.getNonExistingIds());
        return createResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), details);
    }

    // 3. Nhóm các lỗi 400 Bad Request thông thường
    @ExceptionHandler({IllegalArgumentException.class, BadRequestException.class})
    public ResponseEntity<ExceptionResponseDTO> handleBadRequest(RuntimeException ex) {
        return createResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // 4. Lỗi 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponseDTO> handleNotFound(ResourceNotFoundException ex) {
        return createResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // 5. Nhóm các lỗi 401 Unauthorized
    @ExceptionHandler({AuthenticationFailedException.class, UnauthorizedException.class})
    public ResponseEntity<ExceptionResponseDTO> handleUnauthorized(RuntimeException ex) {
        return createResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    // 6. Lỗi 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDTO> handleGeneralException(Exception ex) {
        // Log chi tiết lỗi kèm URI để phục vụ truy vết (tracing)
        log.error("Unhandled exception occurred at {}: ", request.getRequestURI(), ex);
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
                request.getRequestURI() // Đã thêm tham số path vào Record constructor
        );
        return new ResponseEntity<>(response, status);
    }
}