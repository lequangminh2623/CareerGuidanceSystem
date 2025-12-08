package com.lqm.academic_service.repositories;

import com.lqm.academic_service.models.Year;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface YearRepository extends JpaRepository<Year, UUID> {

    @Query("SELECT s FROM Year s " +
            "WHERE (:kw IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', CAST(:kw AS STRING), '%')))")
    Page<Year> findAllByKeyword(@Param("kw") String kw, Pageable pageable);

    boolean existsByNameAndIdNot(String name, UUID id);
}

