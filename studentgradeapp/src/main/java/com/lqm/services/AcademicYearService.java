package com.lqm.services;

import com.lqm.models.AcademicYear;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AcademicYearService {

    Page<AcademicYear> getAcademicYears(Map<String, String> params, Pageable pageable);

    void saveYear(AcademicYear year);

    AcademicYear getYearById(int id);

    void deleteYearById(int id);

    boolean existAcademicYearByYear(String year, Integer excludeId);
}
