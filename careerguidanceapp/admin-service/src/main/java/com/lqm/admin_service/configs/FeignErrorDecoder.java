package com.lqm.admin_service.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lqm.admin_service.dtos.ExceptionResponseDTO;
import com.lqm.admin_service.exceptions.ValidationException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();
    private final ObjectMapper objectMapper;

    public FeignErrorDecoder() {
        this.objectMapper = new ObjectMapper();
        // Đăng ký module để xử lý LocalDateTime trong ExceptionResponseDTO
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        ExceptionResponseDTO responseBody = null;

        try (InputStream bodyIs = response.body().asInputStream()) {
            if (bodyIs != null) {
                responseBody = objectMapper.readValue(bodyIs, ExceptionResponseDTO.class);
            }
        } catch (IOException e) {
            log.error("Không thể parse body lỗi từ Feign: {}", e.getMessage());

            return defaultDecoder.decode(methodKey, response);
        }

        if (responseBody != null) {
            int status = response.status();

            if (status == HttpStatus.BAD_REQUEST.value()) {
                if (responseBody.details() != null) {
                    return new ValidationException(responseBody.message(), responseBody.details());
                } else {
                    return new com.lqm.admin_service.exceptions.BadRequestException(responseBody.message());
                }
            }

            // Có thể xử lý thêm các lỗi khác như 404, 401, 500 nếu muốn...
            // Ví dụ: if (status == 404) return new MyNotFoundException(...);
        }

        return defaultDecoder.decode(methodKey, response);
    }
}