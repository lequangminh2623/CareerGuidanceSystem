package com.lqm.academic_service.repositories;

import com.lqm.academic_service.models.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, UUID>, JpaSpecificationExecutor<Classroom> {

    Page<Classroom> findAllByIdIn(List<UUID> classroomIds, Pageable pageable);

    boolean existsByNameAndGradeIdAndIdNot(String name, UUID gradeId, UUID excludeId);

    @Query("""
        SELECT c
        FROM Classroom c
        LEFT JOIN FETCH c.studentClassroomSet sc
        WHERE c.id = :id
    """)
    Optional<Classroom> findWithStudentsById(@Param("id") UUID id);

}
