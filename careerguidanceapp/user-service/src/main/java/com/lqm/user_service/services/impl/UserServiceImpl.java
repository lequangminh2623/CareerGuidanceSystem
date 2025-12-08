package com.lqm.user_service.services.impl;

import com.lqm.user_service.exceptions.ResourceNotFoundException;
import com.lqm.user_service.models.Role;
import com.lqm.user_service.models.Student;
import com.lqm.user_service.models.User;
import com.lqm.user_service.repositories.StudentRepository;
import com.lqm.user_service.repositories.UserRepository;
import com.lqm.user_service.services.CloudinaryService;
import com.lqm.user_service.services.UserService;
import com.lqm.user_service.specifications.UserSpecification;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final MessageSource messageSource;

    @Override
    public User getUserById(UUID id) {
        return userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                messageSource.getMessage("user.notFound", null, Locale.getDefault()))
        );
    }

    @Override
    @Nonnull
    public UserDetails loadUserByUsername(@Nonnull String email) {
        User u = userRepo.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageSource.getMessage("user.email.invalid", null, Locale.getDefault()))
                );

        return org.springframework.security.core.userdetails.User.withUsername(u.getEmail())
                .password(u.getPassword())
                .roles(u.getRole().name().replace("ROLE_", ""))
                .build();
    }

    @Override
    public Page<User> getUsers(List<UUID> ids, Map<String, String> params, Pageable pageable) {
        Specification<User> paramSpec = UserSpecification.filterByParams(params);
        Specification<User> idSpec = UserSpecification.hasIdIn(ids);
        Specification<User> finalSpec = idSpec.and(paramSpec);

        return userRepo.findAll(finalSpec, pageable);
    }

    @Override
    public User saveUser(User user, MultipartFile file, String code) {
        // Encode password
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(passwordEncoder.encode("1"));
        }

        // Set defaults
        if (user.getId() == null) {
            user.setCreatedDate(LocalDateTime.now());
        }
        if (user.getRole() == null) {
            user.setRole(Role.ROLE_STUDENT);
        }
        if (user.getActive() == null) {
            user.setActive(true);
        }
        user.setUpdatedDate(LocalDateTime.now());

        // STUDENT
        if (Role.ROLE_STUDENT.equals(user.getRole())) {
            Student s = Student.builder().code(code).build();
            if (user.getId() != null) {
                s = studentRepo.findById(user.getId()).orElse(s);
            }

            s.setUser(user);
            user.setStudent(s);
        } else {
            user.setStudent(null);
        }

        // Upload Avatar
        if (file != null && !file.isEmpty()) {
            cloudinaryService.deleteFile(user.getAvatar());
            String avatarUrl = cloudinaryService.uploadFile(file);
            if (avatarUrl != null) {
                user.setAvatar(avatarUrl);
            }
        }

        return userRepo.save(user);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = this.getUserById(id);
        cloudinaryService.deleteFile(user.getAvatar());
        userRepo.deleteById(id);
    }

    @Override
    public boolean existDuplicateUser(String email, UUID excludeId) {
        return userRepo.existsByEmailAndExcludeId(email, excludeId);
    }

    @Override
    public User getCurrentUser(HttpServletRequest request) {
        String email = request.getHeader("X-User-Email");
        if (email == null) return null;

        return userRepo.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("user.notFound", null, Locale.getDefault())
                ));
    }


}

