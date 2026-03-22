package com.coogpath.coogpath.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF protection so Thunder Client can make POST/PUT requests later
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // TEMPORARY: Allow all traffic to pass through without logging in
            );
        return http.build();
    }
}