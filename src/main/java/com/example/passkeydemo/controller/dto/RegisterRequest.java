package com.example.passkeydemo.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.web.webauthn.api.AuthenticatorAttestationResponse;
import org.springframework.security.web.webauthn.api.PublicKeyCredential;

/**
 * {@code POST /api/register} 요청 본문.
 *
 * <p>username과 브라우저가 생성한 credential(attestation 응답)을 함께 받는다.
 */
public record RegisterRequest(

        @NotBlank(message = "username은 필수입니다")
        String username,

        @NotNull(message = "credential은 필수입니다")
        PublicKeyCredential<AuthenticatorAttestationResponse> credential
) {
}
