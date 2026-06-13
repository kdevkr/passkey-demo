package com.example.passkeydemo.controller.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * {@code POST /api/register/options} 요청 본문.
 *
 * <p>타입 없는 {@code Map<String, String>} 대신 검증 가능한 record로 받는다.
 */
public record RegistrationOptionsRequest(

        @NotBlank(message = "username은 필수입니다")
        String username
) {
}
