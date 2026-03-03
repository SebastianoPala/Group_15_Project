package com.unipi.PlayerHive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

// DISABLES SECURITY CHECK, WRITTEN BY GEMINI
@Configuration
public class SecuritySpringBoot {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disabilita CSRF per testare facilmente le POST/DELETE su Postman
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Permette l'accesso a tutti gli endpoint senza login
                );
        return http.build();
    }
}
