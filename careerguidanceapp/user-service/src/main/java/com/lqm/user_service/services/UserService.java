package com.lqm.user_service.services;

import com.lqm.user_service.models.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserService extends UserDetailsService {

    User getUserById(UUID id);

    Page<User> getUsers(List<UUID> ids, Map<String, String> params, Pageable pageable);

    User saveUser(User user, MultipartFile file, String code);

    void deleteUser(UUID id);

    boolean existDuplicateUser(String email, UUID excludeId);

    User getCurrentUser();

    Map<String, Object> getUserStatistics();
}

