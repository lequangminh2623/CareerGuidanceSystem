package com.lqm.admin_service.services;

import com.lqm.admin_service.clients.AuthClient;
import com.lqm.admin_service.dtos.AdminUserLoginDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserDetailsServiceTest {

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private AdminUserDetailsService adminUserDetailsService;

    @Test
    @DisplayName("loadUserByUsername: returns UserDetails when AuthClient succeeds")
    void loadUserByUsername_Success() {
        String email = "admin@test.com";
        AdminUserLoginDTO dto = new AdminUserLoginDTO(email, "hashedPassword", "ADMIN");
        when(authClient.getUserForAuth(email)).thenReturn(dto);

        UserDetails userDetails = adminUserDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo("hashedPassword");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadUserByUsername: throws UsernameNotFoundException when AuthClient throws exception")
    void loadUserByUsername_ClientThrowsException() {
        String email = "unknown@test.com";
        when(authClient.getUserForAuth(email)).thenThrow(new RuntimeException("Service Down"));

        assertThatThrownBy(() -> adminUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found or service down");
    }
}
