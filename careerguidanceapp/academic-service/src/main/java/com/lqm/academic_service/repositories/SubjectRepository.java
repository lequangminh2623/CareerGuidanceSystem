package com.lqm.academic_service.repositories;

import com.lqm.academic_service.models.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    boolean existsByNameAndIdNot(String name, UUID excludeId);

    @Query("SELECT s FROM Subject s " +
            "WHERE (:kw IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', CAST(:kw AS STRING), '%')))")
    Page<Subject> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
}
