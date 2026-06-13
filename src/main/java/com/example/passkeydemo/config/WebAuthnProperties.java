package com.example.passkeydemo.config;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * WebAuthn(Relying Party) 관련 타입 안전 설정.
 *
 * <p>{@code webauthn.*} 프로퍼티를 record 생성자 바인딩으로 주입받는다.
 * 콤마로 구분된 {@code webauthn.allowed-origins} 값은 Spring이 {@link List}로
 * 자동 변환하므로, 더 이상 수동 {@code split(",")} 파싱이 필요 없다.
 */
@ConfigurationProperties(prefix = "webauthn")
public record WebAuthnProperties(

        @DefaultValue("localhost")
        @NonNull String rpId,

        @DefaultValue("Passkey Demo")
        @NonNull String rpName,

        @DefaultValue("https://localhost:8080")
        @NonNull List<String> allowedOrigins
) {
}
