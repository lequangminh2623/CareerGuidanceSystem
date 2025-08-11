package com.lqm.services.impl;

import com.lqm.models.Student;
import com.lqm.repositories.StudentRepository;
import com.lqm.services.StudentService;
import com.lqm.specifications.StudentSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepo;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepo) {
        this.studentRepo = studentRepo;
    }

    @Override
    public Page<Student> getStudents(String kw, String role, Pageable pageable) {
        Specification<Student> spec = StudentSpecification.buildSpecification(kw, role);
        return studentRepo.findAll(spec, pageable);
    }

    @Override
    public Student getStudentByUserId(Integer userId) {
        return studentRepo.findByUserId(userId)
                .orElse(null);
    }

    @Override
    public boolean existsByStudentCode(String code, Integer userId) {
        return studentRepo.existsByCodeAndUserIdNot(code, userId);
    }
}
