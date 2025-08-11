package com.lqm.repositories;

import com.lqm.models.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer>,
        JpaSpecificationExecutor<Student> {

    Optional<Student> findByUserId(Integer userId);

    boolean existsByCodeAndUserIdNot(String code, Integer userId);
}
