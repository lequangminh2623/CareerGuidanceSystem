package com.lqm.user_service.services.impl;

import com.lqm.user_service.repositories.StudentRepository;
import com.lqm.user_service.services.StudentService;import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepo;

    @Override
    public boolean existDuplicateStudent(String code, UUID userId) {
        return studentRepo.existsByCodeAndUserIdNot(code, userId);
    }
}