package com.lqm.services;

import com.lqm.models.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

public interface UserService extends UserDetailsService {

    Optional<User> getUserByEmail(String email);

    boolean authenticate(String email, String password);

    Page<User> getUsers(Map<String, String> params, Pageable pageable);

    User saveUser(User user);

    Optional<User> getUserById(Integer id);

    void deleteUser(Integer id);

    boolean existsByEmail(String email, Integer excludeId);

    User getCurrentUser();
}