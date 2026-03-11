package com.lqm.academic_service.repositories;

import com.lqm.academic_service.models.Grade;
import com.lqm.academic_service.models.GradeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {
    @Query("""
            SELECT g FROM Grade g
            WHERE g.year.id = :yearId
            AND (:kw IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', CAST(:kw AS STRING), '%')))
            ORDER BY g.name
    """)
    List<Grade> findByYearId(
            @Param("yearId") UUID yearId,
            @Param("kw") String kw
    );

    @Query("""
            SELECT g FROM Grade g
            WHERE (:kw IS NULL OR LOWER(CONCAT(g.year.name, ' - ', g.name))
            LIKE LOWER(CONCAT('%', CAST(:kw AS STRING), '%')))
            ORDER BY g.year.name desc, g.name
    """)
    List<Grade> findAllByKeyword(@Param("kw") String kw);

    boolean existsByNameAndYearIdAndIdNot(
            GradeType name,
            UUID yearId,
            UUID id
    );
}