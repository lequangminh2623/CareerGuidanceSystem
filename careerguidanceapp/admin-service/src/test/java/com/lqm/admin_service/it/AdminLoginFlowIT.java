package com.lqm.admin_service.it;

import com.lqm.admin_service.clients.*;
import com.lqm.admin_service.dtos.AdminUserLoginDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AdminLoginFlowIT — Security Flow Tests")
class AdminLoginFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // We mock all Feign Clients to isolate this service
    @MockitoBean
    private AuthClient authClient;
    @MockitoBean
    private DeviceClient deviceClient;
    @MockitoBean
    private SectionClient sectionClient;
    @MockitoBean
    private ClassroomClient classroomClient;
    @MockitoBean
    private UserClient userClient;
    @MockitoBean
    private SubjectClient subjectClient;
    @MockitoBean
    private FingerprintClient fingerprintClient;
    @MockitoBean
    private AttendanceClient attendanceClient;
    @MockitoBean
    private SemesterClient semesterClient;
    @MockitoBean
    private CurriculumClient curriculumClient;
    @MockitoBean
    private GradeClient gradeClient;
    @MockitoBean
    private TranscriptClient transcriptClient;
    @MockitoBean
    private YearClient yearClient;

    @Test
    @DisplayName("Truy cập trang bảo vệ khi chưa đăng nhập -> chuyển hướng về http://localhost/login")
    void accessProtectedResource_WithoutSession_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Đăng nhập thành công với tài khoản Admin -> chuyển hướng về /")
    void loginSuccess_RedirectsToRoot() throws Exception {
        String adminEmail = "admin@ou.edu.vn";
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        AdminUserLoginDTO mockedUser = new AdminUserLoginDTO(adminEmail, encodedPassword, "ADMIN");
        when(authClient.getUserForAuth(eq(adminEmail))).thenReturn(mockedUser);

        mockMvc.perform(formLogin("/login")
                .user(adminEmail)
                .password(rawPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("Đăng nhập thất bại do sai mật khẩu -> chuyển hướng về /login?error=true")
    void loginFailed_WrongPassword_RedirectsToError() throws Exception {
        String adminEmail = "admin@ou.edu.vn";
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        AdminUserLoginDTO mockedUser = new AdminUserLoginDTO(adminEmail, encodedPassword, "ADMIN");
        when(authClient.getUserForAuth(eq(adminEmail))).thenReturn(mockedUser);

        mockMvc.perform(formLogin("/login")
                .user(adminEmail)
                .password("wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @DisplayName("Đăng nhập thất bại do user không tồn tại -> chuyển hướng về /login?error=true")
    void loginFailed_UserNotFound_RedirectsToError() throws Exception {
        String adminEmail = "nonexistent@ou.edu.vn";

        when(authClient.getUserForAuth(eq(adminEmail)))
                .thenThrow(new RuntimeException("Not Found"));

        mockMvc.perform(formLogin("/login")
                .user(adminEmail)
                .password("password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }
}
