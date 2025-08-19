/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.configs;

import com.lqm.filters.JwtFilter;
import com.lqm.validators.AcademicYearValidator;
import com.lqm.validators.ClassroomValidator;
import com.lqm.validators.CourseValidator;
import com.lqm.validators.ForumPostDTOValidator;
import com.lqm.validators.ForumPostValidator;
import com.lqm.validators.ForumReplyDTOValidator;
import com.lqm.validators.ForumReplyValidator;
import com.lqm.validators.SemesterValidator;
import com.lqm.validators.GradeValidator;
import com.lqm.validators.UserDTOValidator;
import com.lqm.validators.UserValidator;
import com.lqm.validators.WebAppValidator;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 *
 * @author Le Quang Minh
 */
@Configuration
@EnableWebSecurity
@EnableTransactionManagement
@ComponentScan(basePackages = {
        "com.lqm.controllers",
        "com.lqm.repositories",
        "com.lqm.services",
        "com.lqm.validators",
        "com.lqm.utils"
})
@RequiredArgsConstructor
public class SecurityConfig {

    private final ClassroomValidator classroomValidator;
    private final UserValidator userValidator;
    private final GradeValidator gradeValidator;
    private final CourseValidator courseValidator;
    private final AcademicYearValidator academicYearValidator;
    private final SemesterValidator semesterValidator;
    private final ForumPostValidator forumPostValidator;
    private final ForumReplyValidator forumReplyValidator;
    private final UserDTOValidator userDTOValidator;
    private final ForumPostDTOValidator forumPostDTOValidator;
    private final ForumReplyDTOValidator forumReplyDTOValidator;

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login", "/api/users", "/api/auth/**").permitAll()
                        .requestMatchers("/api/secure/grades/student").hasRole("STUDENT")
                        .requestMatchers("/api/secure/classrooms", "/api/secure/classrooms/*/forums",
                                "/api/secure/ai/ask", "/api/secure/forums/**").hasAnyRole("LECTURER", "STUDENT")
                        .requestMatchers("/api/secure/ai/analysis/**", "/api/secure/classrooms/**").hasRole("LECTURER")
                        .requestMatchers("/api/secure/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Unauthorized\"}");
                        })
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/access-deny").permitAll()
                        .requestMatchers("/", "/home", "/users", "/users/**", "/classrooms", "/classrooms/**",
                                "/courses", "/courses/**", "/years", "/years/**",
                                "/forums", "/forums/**", "/replies", "/replies/**").hasRole("ADMIN")
                )
                .formLogin(form -> form.loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true").permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login").permitAll())
                .exceptionHandling(e -> e.accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }

//    @Bean
//    @Order(0)
//    public StandardServletMultipartResolver multipartResolver() {
//        return new StandardServletMultipartResolver();
//    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource resource
                = new ResourceBundleMessageSource();
        resource.setBasename("messages");
        resource.setDefaultEncoding("UTF-8");
        return resource;
    }

    @Bean
    public jakarta.validation.Validator validator() {
        LocalValidatorFactoryBean bean
                = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }

    @Bean
    public WebAppValidator webAppValidator(
            jakarta.validation.Validator beanValidator,
            ClassroomValidator classroomValidator,
            UserValidator userValidator,
            CourseValidator courseValidator,
            AcademicYearValidator academicYearValidator,
            SemesterValidator semesterValidator,
            ForumPostValidator forumPostValidator,
            ForumReplyValidator forumReplyValidator,
            GradeValidator gradeValidator,
            UserDTOValidator userDTOValidator,
            ForumPostDTOValidator forumPostDTOValidator,
            ForumReplyDTOValidator forumReplyDTOValidator
    ) {
        Set<Validator> springValidators = new HashSet<>();
        springValidators.add(classroomValidator);
        springValidators.add(userValidator);
        springValidators.add(courseValidator);
        springValidators.add(academicYearValidator);
        springValidators.add(semesterValidator);
        springValidators.add(forumPostValidator);
        springValidators.add(forumReplyValidator);
        springValidators.add(gradeValidator);
        springValidators.add(userDTOValidator);
        springValidators.add(forumPostDTOValidator);
        springValidators.add(forumReplyDTOValidator);

        return new WebAppValidator(beanValidator, springValidators);
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:3000/"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl();
        handler.setErrorPage("/access-deny");
        return handler;
    }

}