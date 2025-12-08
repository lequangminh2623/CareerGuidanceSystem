package com.lqm.academic_service.services;

import com.lqm.academic_service.models.Year;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface YearService {

    Page<Year> getYears(Map<String, String> params, Pageable pageable);

    Year saveYear(Year year);

    Year getYearById(UUID id);

    void deleteYearById(UUID id);

    boolean existDuplicateYear(String name, UUID excludeId);

    boolean existYearById(UUID yearId);
}
