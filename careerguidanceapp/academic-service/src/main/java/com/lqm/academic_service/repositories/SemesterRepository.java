package com.lqm.academic_service.repositories;

import com.lqm.academic_service.models.Semester;
import com.lqm.academic_service.models.SemesterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface SemesterRepository extends JpaRepository<Semester, UUID> {

    @Query("""
            SELECT s FROM Semester s
            WHERE s.year.id = :yearId
            AND (:kw IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', CAST(:kw AS STRING), '%')))
            ORDER BY s.name
    """)
    List<Semester> findByYearId(@Param("yearId") UUID yearId, @Param("kw") String kw);

    @Query("""
            SELECT s FROM Semester s
            WHERE (:kw IS NULL OR LOWER(CONCAT(s.year.name, ' - ', s.name))
            LIKE LOWER(CONCAT('%', CAST(:kw AS STRING), '%')))
            ORDER BY s.year.name desc, s.name
    """)
    List<Semester> findAllByKeyword(@Param("kw") String kw);

    boolean existsByNameAndYearIdAndIdNot(SemesterType name, UUID yearId, UUID id);
}
