package com.rprelevic.xm.recom.cfg;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // TODO: Remove API (just for testing)
                                .requestMatchers("/api/**","/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // Allow access to H2 console and Swagger UI
                                .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF protection for H2 console
                .headers(headers ->
                        headers.frameOptions(FrameOptionsConfig::disable)) // Disable frame options for H2 console
                .httpBasic(withDefaults()) // Enable basic authentication
                .build();
    }
}