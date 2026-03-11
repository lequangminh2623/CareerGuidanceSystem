package com.lqm.admin_service.services;

import com.lqm.admin_service.clients.AuthClient;
import com.lqm.admin_service.dtos.AdminUserLoginDTO;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AuthClient authClient;

    @Override
    @Nonnull
    public UserDetails loadUserByUsername(@Nonnull String email) throws UsernameNotFoundException {
        try {
            AdminUserLoginDTO response = authClient.getUserForAuth(email);
            
            return org.springframework.security.core.userdetails.User.withUsername(response.email())
                    .password(response.password())
                    .roles(response.role())
                    .build();
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found or service down");
        }
    }
}