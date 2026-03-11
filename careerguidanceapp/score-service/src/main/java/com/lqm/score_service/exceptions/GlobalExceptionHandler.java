package com.lqm.score_service.exceptions;

import com.lqm.score_service.dtos.ExceptionResponseDTO;
import lombok.RequiredArgsConstructor;
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
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    // 1. Xử lý lỗi Validation (400)
    // Chuyển đổi: [{"field": "a", "message": "b"}] -> {"a": "b"}
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

    // 2. Xử lý NonExistingUsersException (400)
    // Trả về danh sách ID bị thiếu trong field 'details'
    @ExceptionHandler(NonExistingUsersException.class)
    public ResponseEntity<ExceptionResponseDTO> handleNonExistingUsersException(NonExistingUsersException ex) {
        Map<String, Object> details = Map.of("nonExistingUserIds", ex.getNonExistingIds());
        return createResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), details);
    }

    // 3. Nhóm các lỗi 400 Bad Request chung (Logic, Argument...)
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

    // 7. Lỗi 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDTO> handleGeneralException(Exception ex) {
        // Nên log lỗi ra console tại đây để debug
        ex.printStackTrace();
        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error: " + ex.getMessage(), null);
    }

    // --- HELPER METHOD (Factory method để tạo Response) ---
    private ResponseEntity<ExceptionResponseDTO> createResponse(HttpStatus status, String message, Object details) {
        ExceptionResponseDTO response = new ExceptionResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(), // Tự động lấy text chuẩn (VD: "Bad Request")
                message,
                details
        );
        return new ResponseEntity<>(response, status);
    }
}