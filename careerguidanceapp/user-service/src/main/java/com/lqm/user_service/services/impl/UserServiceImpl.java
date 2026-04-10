package com.lqm.user_service.services.impl;

import com.lqm.user_service.exceptions.ResourceNotFoundException;
import com.lqm.user_service.exceptions.UnauthorizedException;
import com.lqm.user_service.models.Role;
import com.lqm.user_service.models.Student;
import com.lqm.user_service.models.User;
import com.lqm.user_service.repositories.StudentRepository;
import com.lqm.user_service.repositories.UserRepository;
import com.lqm.user_service.services.CloudinaryService;
import com.lqm.user_service.services.UserService;
import com.lqm.user_service.specifications.UserSpecification;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final MessageSource messageSource;
    private final UserRepository userRepository;

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
    @Transactional(readOnly = true)
    public Page<User> getUsers(List<UUID> ids, Map<String, String> params, Pageable pageable) {
        Specification<User> spec = UserSpecification.filterByParams(params).and(UserSpecification.hasIdIn(ids));
        return userRepo.findAll(spec, pageable);
    }

    @Override
    public User saveUser(User user, MultipartFile file, String code) {
        // Encode password
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(passwordEncoder.encode("1"));
        }

        if (user.getRole() == null) {
            user.setRole(Role.ROLE_STUDENT);
        }
        if (user.getActive() == null) {
            user.setActive(true);
        }

        // STUDENT
        if (Role.ROLE_STUDENT.equals(user.getRole())) {
            Student s = new Student();
            if (user.getId() != null) {
                s = studentRepo.findById(user.getId()).orElse(s);
            }
            s.setCode(code);
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
    public User getCurrentUser() {
        String email = (String) Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                .getPrincipal();

        if (email == null) {
            throw new UnauthorizedException(
                    messageSource.getMessage("user.notLoggedIn", null, Locale.getDefault())
            );
        }

        return userRepo.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("user.notFound", null, Locale.getDefault())
                ));
    }

    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.countBy());

        stats.put("byRole", userRepository.countUserByRole().stream()
                .collect(Collectors.toMap(row -> row[0].toString(), row -> row[1])));

        stats.put("byStatus", userRepository.countUserByStatus().stream()
                .collect(Collectors.toMap(row -> (Boolean) row[0] ? "active" : "deactive", row -> row[1])));

        stats.put("studentGrowth", userRepository.countStudentGrowthByYear().stream()
                .collect(Collectors.toMap(row -> row[0].toString(), row -> row[1])));

        return stats;

    }

}

