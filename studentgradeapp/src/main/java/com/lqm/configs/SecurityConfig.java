/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.configs;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

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
public class SecurityConfig {

    @Autowired
    private ClassroomValidator classroomValidator;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private GradeValidator gradeValidator;

    @Autowired
    private CourseValidator courseValidator;

    @Autowired
    private AcademicYearValidator academicYearValidator;

    @Autowired
    private SemesterValidator semesterValidator;

    @Autowired
    private ForumPostValidator forumPostValidator;

    @Autowired
    private ForumReplyValidator forumReplyValidator;

    @Autowired
    private UserDTOValidator userDTOValidator;

    @Autowired
    private ForumPostDTOValidator forumPostDTOValidator;

    @Autowired
    private ForumReplyDTOValidator forumReplyDTOValidator;

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
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
                .csrf(c -> c.disable()).authorizeHttpRequests(auth -> auth
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
//    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
//        return new HandlerMappingIntrospector();
//    }

    @Bean
    @Order(0)
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
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
    public WebAppValidator webAppValidator() {
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
        WebAppValidator validator = new WebAppValidator();
        validator.setSpringValidators(springValidators);
        return validator;
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