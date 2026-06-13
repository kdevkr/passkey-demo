package com.example.passkeydemo.controller;

import com.example.passkeydemo.controller.dto.RegisterRequest;
import com.example.passkeydemo.controller.dto.RegistrationOptionsRequest;
import com.example.passkeydemo.exception.RegistrationException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.PublicKeyCredential;
import org.springframework.security.web.webauthn.api.AuthenticatorAttestationResponse;
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
@RequestMapping(path = "/api/register", version = "1.0")
public class WebAuthnRegistrationController {

    private final WebAuthnRelyingPartyOperations relyingPartyOperations;
    private final PublicKeyCredentialUserEntityRepository userEntityRepository;

    public WebAuthnRegistrationController(
            WebAuthnRelyingPartyOperations relyingPartyOperations,
            PublicKeyCredentialUserEntityRepository userEntityRepository) {
        this.relyingPartyOperations = relyingPartyOperations;
        this.userEntityRepository = userEntityRepository;
    }

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
    public ResponseEntity<PublicKeyCredentialCreationOptions> getRegistrationOptions(
            @Valid @RequestBody RegistrationOptionsRequest request) {
        String username = request.username();

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

    @PostMapping
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        String username = request.username();
        PublicKeyCredential<AuthenticatorAttestationResponse> credential = request.credential();

        PublicKeyCredentialCreationOptions options = optionsStore.get(username);
        if (options == null) {
            throw new RegistrationException(
                    "등록 세션을 찾을 수 없습니다. 먼저 옵션을 요청하세요.");
        }

        try {
            RelyingPartyPublicKey relyingPartyPublicKey = new RelyingPartyPublicKey(credential, "My Passkey");
            ImmutableRelyingPartyRegistrationRequest registrationRequest = new ImmutableRelyingPartyRegistrationRequest(options, relyingPartyPublicKey);
            relyingPartyOperations.registerCredential(registrationRequest);

            // Clean up options after successful registration
            optionsStore.remove(username);
            return ResponseEntity.ok("Registration successful");
        } catch (RegistrationException e) {
            throw e;
        } catch (Exception e) {
            throw new RegistrationException("패스키 등록 검증에 실패했습니다.", e);
        }
    }
}
