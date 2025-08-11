package com.lqm.services;

import com.lqm.models.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {

    Page<Student> getStudents(String kw, String role, Pageable pageable);

    Student getStudentByUserId(Integer userId);

    boolean existsByStudentCode(String code, Integer userId);
}
