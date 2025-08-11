package com.lqm.repositories;

import com.lqm.models.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Integer> {

    Page<AcademicYear> findByYearContainingIgnoreCase(String year, Pageable pageable);

    boolean existsByYear(String year);

    boolean existsByYearAndIdNot(String year, Integer id);
}

