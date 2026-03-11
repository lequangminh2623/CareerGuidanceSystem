package com.lqm.academic_service.configs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.academic_service.exceptions.NonExistingUsersException;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {

        if (response.status() == HttpStatus.BAD_REQUEST.value()) {

            try (InputStream is = response.body().asInputStream()) {
                Map<String, Object> errorBody = mapper.readValue(is, new TypeReference<>() {});

                Object detailsObj = errorBody.get("details");
                if (detailsObj instanceof Map<?, ?> details) {

                    Object listObj = details.get("nonExistingUserIds");
                    if (listObj instanceof List<?> rawList) {
                        List<UUID> nonExistingIds = rawList.stream()
                                .filter(item -> item instanceof String)
                                .map(item -> UUID.fromString((String) item))
                                .collect(Collectors.toList());

                        return new NonExistingUsersException(nonExistingIds);
                    }
                }

                return FeignException.errorStatus(methodKey, response);

            } catch (Exception e) {
                return new RuntimeException(e);
            }
        }

        return defaultDecoder.decode(methodKey, response);
    }
}