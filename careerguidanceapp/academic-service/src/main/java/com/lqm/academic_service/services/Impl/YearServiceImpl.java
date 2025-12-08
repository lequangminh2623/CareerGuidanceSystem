package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.YearRequestDTO;
import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.models.Year;
import com.lqm.academic_service.repositories.YearRepository;
import com.lqm.academic_service.services.YearService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class YearServiceImpl implements YearService {
    
    private final YearRepository yearRepo;
    private final MessageSource messageSource;

    @Override
    public Page<Year> getYears(Map<String, String> params, Pageable pageable) {
        String kw = (params != null) ? params.get("kw") : null;
        return yearRepo.findAllByKeyword(kw, pageable);
    }

    @Override
    public Year saveYear(Year year) {
        return yearRepo.save(year);
    }

    @Override
    public Year getYearById(UUID id) {
        return yearRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                messageSource.getMessage("year.notFound", null, Locale.getDefault()))
        );
    }

    @Override
    public void deleteYearById(UUID id) {
        yearRepo.deleteById(id);
    }

    @Override
    public boolean existDuplicateYear(String name, UUID excludeId) {
        return yearRepo.existsByNameAndIdNot(name, excludeId);
    }

    @Override
    public boolean existYearById(UUID yearId) {
        return yearRepo.existsById(yearId);
    }
}
