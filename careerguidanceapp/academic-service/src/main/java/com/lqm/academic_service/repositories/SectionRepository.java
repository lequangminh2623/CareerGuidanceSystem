package com.lqm.academic_service.repositories;

import com.lqm.academic_service.models.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID>, JpaSpecificationExecutor<Section> {

        boolean existsByTeacherIdAndId(UUID teacherId, UUID id);

        @Query("SELECT s.curriculum.id, s.id FROM Section s " +
                        "WHERE s.classroom.id = :classroomId " +
                        "AND s.curriculum.id IN :curriculumIds")
        List<Object[]> findCurriculumAndSectionIds(@Param("classroomId") UUID classroomId,
                        @Param("curriculumIds") Set<UUID> curriculumIds);

        List<Section> findByTeacherId(UUID teacherId);
}
