package com.example.passkeydemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.webauthn.api.AuthenticatorAttestationResponse;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.PublicKeyCredential;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.ImmutableRelyingPartyRegistrationRequest;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.RelyingPartyPublicKey;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/register")
public class WebAuthnRegistrationController {

    @Autowired
    private WebAuthnRelyingPartyOperations relyingPartyOperations;

    @Autowired
    private PublicKeyCredentialUserEntityRepository userEntityRepository;

    private final Map<String, PublicKeyCredentialCreationOptions> optionsStore = new ConcurrentHashMap<>();

    private static class SimpleUserEntity implements PublicKeyCredentialUserEntity {
        private final byte[] id;
        private final String name;
        private final String displayName;

        public SimpleUserEntity(byte[] id, String name, String displayName) {
            this.id = id;
            this.name = name;
            this.displayName = displayName;
        }

        @Override
        public Bytes getId() {
            return new Bytes(id);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }
    }

    @PostMapping("/options")
    public ResponseEntity<?> getRegistrationOptions(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username is required");
        }

        PublicKeyCredentialUserEntity userEntity = userEntityRepository.findByUsername(username);
        if (userEntity == null) {
            byte[] userId = new byte[32];
            new SecureRandom().nextBytes(userId);
            userEntity = new SimpleUserEntity(userId, username, username);
            userEntityRepository.save(userEntity);
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username, null, AuthorityUtils.NO_AUTHORITIES);
        
        PublicKeyCredentialCreationOptions options = relyingPartyOperations
                .createPublicKeyCredentialCreationOptions(
                        () -> authentication
                );

        optionsStore.put(username, options);

        return ResponseEntity.ok(options);
    }

    public static class RegisterRequest {
        private String username;
        private PublicKeyCredential<AuthenticatorAttestationResponse> credential;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public PublicKeyCredential<AuthenticatorAttestationResponse> getCredential() {
            return credential;
        }

        public void setCredential(PublicKeyCredential<AuthenticatorAttestationResponse> credential) {
            this.credential = credential;
        }
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String username = request.getUsername();
        PublicKeyCredential<AuthenticatorAttestationResponse> credential = request.getCredential();

        if (username == null || credential == null) {
            return ResponseEntity.badRequest().body("Username and credential are required");
        }

        PublicKeyCredentialCreationOptions options = optionsStore.get(username);
        if (options == null) {
            return ResponseEntity.badRequest().body("No registration session found for user. Please request options first.");
        }

        try {
            RelyingPartyPublicKey relyingPartyPublicKey = new RelyingPartyPublicKey(credential, "My Passkey");
            ImmutableRelyingPartyRegistrationRequest registrationRequest = new ImmutableRelyingPartyRegistrationRequest(options, relyingPartyPublicKey);
            relyingPartyOperations.registerCredential(registrationRequest);

            // Clean up options after successful registration
            optionsStore.remove(username);
            return ResponseEntity.ok("Registration successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration verification failed: " + e.getMessage());
        }
    }
}
