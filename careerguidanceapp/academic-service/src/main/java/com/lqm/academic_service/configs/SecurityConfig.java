/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.academic_service.configs;

import com.lqm.academic_service.filters.AuthFilter;
import com.lqm.academic_service.models.Role;
import com.lqm.academic_service.validators.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 *
 * @author Le Quang Minh
 */
@Configuration
@EnableWebSecurity
@EnableTransactionManagement
@ComponentScan(basePackages = {
        "com.lqm.academic_service.clients",
        "com.lqm.academic_service.controllers",
        "com.lqm.academic_service.repositories",
        "com.lqm.academic_service.services",
        "com.lqm.academic_service.validators",
        "com.lqm.academic_service.utils",
        "com.lqm.academic_service.mappers",
        "com.lqm.academic_service.filters"
})
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthFilter headerFilter) {

        http.securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) )

                .addFilterBefore(headerFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/secure/**").authenticated()
                        .requestMatchers("/api/internal/admin/**").hasRole(Role.ROLE_ADMIN.getRoleName().toUpperCase())
                        .anyRequest().authenticated()
                );

        http.exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"error\":\"Unauthorized\", \"message\":\"Token validation required\"}");
                })
        );

        return http.build();
    }

    @Bean
    public AuthFilter authFilter() {
        return new AuthFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

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
            YearRequestDTOValidator yearRequestDTOValidator,
            SemesterRequestDTOValidator semesterRequestDTOValidator,
            GradeRequestDTOValidator gradeRequestDTOValidator,
            SubjectRequestDTOValidator subjectRequestDTOValidator,
            ClassroomRequestDTOValidator classroomRequestDTOValidator,
            CurriculumRequestDTOValidator curriculumRequestDTOValidator,
            SectionListRequestValidator sectionListRequestValidator
    ) {
        Set<Validator> springValidators = new HashSet<>();
        springValidators.add(yearRequestDTOValidator);
        springValidators.add(semesterRequestDTOValidator);
        springValidators.add(gradeRequestDTOValidator);
        springValidators.add(subjectRequestDTOValidator);
        springValidators.add(classroomRequestDTOValidator);
        springValidators.add(curriculumRequestDTOValidator);
        springValidators.add(sectionListRequestValidator);

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