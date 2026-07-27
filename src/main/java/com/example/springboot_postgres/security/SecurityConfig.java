package com.example.springboot_postgres.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers {@link JwtAuthFilter} for the CRUD endpoints only. Health,
 * version, actuator and Swagger paths are not listed here, so they stay open.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilter() {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(new JwtAuthFilter());
        registration.addUrlPatterns("/user", "/user/*", "/posts", "/posts/*");
        registration.setName("jwtAuthFilter");
        return registration;
    }
}
