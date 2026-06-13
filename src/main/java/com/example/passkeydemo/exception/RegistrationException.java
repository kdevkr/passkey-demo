package com.example.passkeydemo.exception;

/**
 * 패스키 등록 과정에서 발생하는 도메인 예외.
 *
 * <p>WebAuthn 라이브러리의 내부 예외를 감싸, 클라이언트에는 안전한 메시지만
 * 노출하고 원인(cause)은 서버 로그에만 남기기 위한 용도다.
 */
public class RegistrationException extends RuntimeException {

    public RegistrationException(String message) {
        super(message);
    }

    public RegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
