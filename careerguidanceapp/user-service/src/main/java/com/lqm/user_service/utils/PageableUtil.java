package com.lqm.user_service.utils;

import com.lqm.user_service.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class PageableUtil {

    private final MessageSource messageSource;

    public Pageable getPageable(String pageParam, int pageSize, List<String> sortFields) {
        // Xử lý Sort
        Sort sort = Sort.unsorted();
        if (sortFields != null && !sortFields.isEmpty()) {
            List<Sort.Order> orders = sortFields.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(s -> {
                        String[] parts = s.split(":");
                        String property = parts[0].trim();
                        Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC;
                        return new Sort.Order(direction, property);
                    })
                    .toList();
            sort = Sort.by(orders);
        }

        // Xử lý pageParam
        if (pageParam == null || pageParam.isBlank()) {
            return PageRequest.of(0, Integer.MAX_VALUE, sort);
        }

        int pageNumber;
        try {
            int parsedPage = Integer.parseInt(pageParam.trim());
            if (parsedPage <= 0) {
                throw new BadRequestException(
                        messageSource.getMessage("pageable.invalid", null, Locale.getDefault())
                );
            }
            pageNumber = parsedPage - 1;
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                    messageSource.getMessage("pageable.invalid", null, Locale.getDefault())
            );
        }

        return PageRequest.of(pageNumber, pageSize, sort);
    }


}
