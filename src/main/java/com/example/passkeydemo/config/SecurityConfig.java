package com.example.passkeydemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for simple REST API testing
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll() // Allow everyone to access all endpoints for demo
            )
            .webAuthn(webauthn -> webauthn
                .rpId("localhost")
                .rpName("Passkey Demo")
                .allowedOrigins("https://localhost:5173")
            )
            .formLogin(withDefaults()); // Optional: keep form login active for fallback

        return http.build();
    }
}
