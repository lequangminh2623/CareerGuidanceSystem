package com.lqm.repositories;

import com.lqm.models.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SemesterRepository extends JpaRepository<Semester, Integer>, JpaSpecificationExecutor<Semester> {

    @Query("SELECT s FROM Semester s " +
            "WHERE s.academicYear.id = :yearId " +
            "AND (:kw IS NULL OR LOWER(s.semesterType) LIKE LOWER(CONCAT('%', :kw, '%'))) ")
    List<Semester> findByAcademicYearId(
            @Param("yearId") int yearId,
            @Param("kw") String kw
    );

    @Query("SELECT s FROM Semester s " +
            "WHERE (:kw IS NULL OR LOWER(CONCAT(s.academicYear.year, ' - ', s.semesterType)) LIKE LOWER(CONCAT('%', :kw, '%')))")
    List<Semester> findAllByKeyword(@Param("kw") String kw);

    boolean existsBySemesterTypeAndAcademicYearId(
            String type,
            Integer yearId
    );

    boolean existsBySemesterTypeAndAcademicYearIdAndIdNot(
            String type,
            Integer yearId,
            Integer id
    );
}
