package com.lqm.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lqm.models.ForumPost;
import com.lqm.models.User;
import com.lqm.repositories.UserRepository;
import com.lqm.services.UserService;
import com.lqm.specifications.CourseSpecification;
import com.lqm.specifications.ForumPostSpecification;
import com.lqm.specifications.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String email) {
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email!"));

        return org.springframework.security.core.userdetails.User.withUsername(u.getEmail())
                .password(u.getPassword())
                .roles(u.getRole().replace("ROLE_", ""))
                .build();
    }

    @Override
    public boolean authenticate(String email, String password) {
        return userRepo.findByEmail(email)
                .map(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElse(false);
    }

    @Override
    public Page<User> getUsers(Map<String, String> params, Pageable pageable) {
        return userRepo.findAll(
                UserSpecification.filterByParams(params),
                pageable
        );
    }

    @Override
    public User saveUser(User user) {
        // Encode password
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        // Set defaults
        if (user.getCreatedDate() == null) {
            user.setCreatedDate(new Date());
        }
        if (user.getRole() == null) {
            user.setRole("ROLE_STUDENT");
        }
        // Upload avatar
        if (user.getFile() != null && !user.getFile().isEmpty()) {
            try {
                var res = cloudinary.uploader().upload(
                        user.getFile().getBytes(),
                        ObjectUtils.asMap("resource_type", "auto", "folder", "GradeManagement")
                );
                user.setAvatar(res.get("secure_url").toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        user.setUpdatedDate(new Date());
        return userRepo.save(user);
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        return userRepo.findById(id);
    }

    @Override
    public void deleteUser(Integer id) {
        userRepo.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email, Integer excludeId) {
        return userRepo.existsByEmailAndExcludeId(email, excludeId);
    }

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepo.findByEmail(email).orElse(null);
    }
}
