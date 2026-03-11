package com.lqm.user_service.services;

import java.util.UUID;

public interface StudentService {

    boolean existDuplicateStudent(String code, UUID userId);

    boolean existStudentById(UUID id);
}
