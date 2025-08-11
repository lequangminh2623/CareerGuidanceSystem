package com.lqm.services.impl;

import com.lqm.models.AcademicYear;
import com.lqm.repositories.AcademicYearRepository;
import com.lqm.services.AcademicYearService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class AcademicYearServiceImpl implements AcademicYearService {

    @Autowired
    private AcademicYearRepository academicYearRepo;

    @Override
    public Page<AcademicYear> getAcademicYears(Map<String, String> params, Pageable pageable) {
        String kw = params.get("kw");
        if (kw != null && !kw.isEmpty()) {
            return academicYearRepo.findByYearContainingIgnoreCase(kw, pageable);
        }
        return academicYearRepo.findAll(pageable);
    }

    @Override
    public void saveYear(AcademicYear year) {
        academicYearRepo.save(year);
    }

    @Override
    public AcademicYear getYearById(int id) {
        return academicYearRepo.findById(id).orElse(null);
    }

    @Override
    public void deleteYearById(int id) {
        academicYearRepo.deleteById(id);
    }

    @Override
    public boolean existAcademicYearByYear(String year, Integer excludeId) {
        if (excludeId != null && excludeId > 0) {
            return academicYearRepo.existsByYearAndIdNot(year, excludeId);
        }
        return academicYearRepo.existsByYear(year);
    }
}
