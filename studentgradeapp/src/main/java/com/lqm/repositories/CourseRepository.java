package com.lqm.repositories;

import com.lqm.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository
        extends JpaRepository<Course, Integer>,
        JpaSpecificationExecutor<Course> {
    boolean existsByNameAndIdNot(String name, Integer excludeId);
}
