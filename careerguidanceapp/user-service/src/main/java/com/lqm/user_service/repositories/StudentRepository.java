package com.lqm.user_service.repositories;

import com.lqm.user_service.models.Student;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID>,
        JpaSpecificationExecutor<Student> {

    boolean existsByCodeAndUserIdNot(String code, UUID userId);
}

