package com.example.passkeydemo.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;
import org.springframework.security.web.webauthn.jackson.WebauthnJacksonModule;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.security.web.webauthn.management.Webauthn4JRelyingPartyOperations;

import java.util.Set;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(WebAuthnProperties.class)
public class SecurityConfig {

    private final WebAuthnProperties properties;

    public SecurityConfig(WebAuthnProperties properties) {
        this.properties = properties;
    }

    @Bean
    public UserDetailsService userDetailsService(PublicKeyCredentialUserEntityRepository userEntityRepository) {
        return username -> {
            PublicKeyCredentialUserEntity userEntity = userEntityRepository.findByUsername(username);
            if (userEntity == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
            return User.withUsername(username)
                    .password("{noop}")
                    .roles("USER")
                    .build();
        };
    }

    @Bean
    public WebauthnJacksonModule webauthnJacksonModule() {
        return new WebauthnJacksonModule();
    }


    @Bean
    public WebAuthnRelyingPartyOperations relyingPartyOperations(
            PublicKeyCredentialUserEntityRepository userEntityRepository,
            UserCredentialRepository userCredentialRepository) {
        PublicKeyCredentialRpEntity rpEntity = PublicKeyCredentialRpEntity.builder()
                .id(properties.rpId())
                .name(properties.rpName())
                .build();

        Set<String> origins = Set.copyOf(properties.allowedOrigins());

        return new Webauthn4JRelyingPartyOperations(
                userEntityRepository,
                userCredentialRepository,
                rpEntity,
                origins
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] originsArray = properties.allowedOrigins().toArray(String[]::new);

        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for simple REST API testing
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll() // Allow everyone to access all endpoints for demo
            )
            .webAuthn(webauthn -> webauthn
                .rpId(properties.rpId())
                .rpName(properties.rpName())
                .allowedOrigins(originsArray)
            )
            .formLogin(withDefaults()); // Optional: keep form login active for fallback

        return http.build();
    }
}
