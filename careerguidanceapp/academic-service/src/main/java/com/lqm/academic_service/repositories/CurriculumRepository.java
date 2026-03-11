package com.lqm.academic_service.repositories;

import com.lqm.academic_service.models.Curriculum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CurriculumRepository extends JpaRepository<Curriculum, UUID>, JpaSpecificationExecutor<Curriculum> {
    boolean existsByGradeIdAndSemesterIdAndSubjectIdAndIdNot(
            UUID gradeId, UUID semesterId, UUID subjectId, UUID excludeId
    );
}
