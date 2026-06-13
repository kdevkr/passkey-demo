package com.example.passkeydemo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 전역 예외 → RFC 9457 {@link ProblemDetail} 변환 핸들러.
 *
 * <p>
 * 컨트롤러는 도메인 의미의 예외만 던지고, "어떤 HTTP 응답으로 바꿀지"는
 * 이곳 한 곳에 모은다. 응답 본문은 {@code application/problem+json} 표준 포맷이다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Bean Validation(@Valid) 실패 처리.
     *
     * <p>
     * 필드별 오류 메시지를 {@code errors} 확장 속성에 담아 클라이언트가
     * 어떤 입력이 왜 틀렸는지 알 수 있게 한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "요청 본문 검증에 실패했습니다.");
        problem.setTitle("Validation Failed");

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("errors", errors);
        return problem;
    }

    /**
     * 패스키 등록 도메인 예외 처리.
     *
     * <p>
     * 클라이언트에는 안전하게 작성된 {@code ex.getMessage()}만 {@code detail}로 노출하고,
     * 원인(cause)이 있으면 전체 스택트레이스를 서버 로그에만 남긴다. 내부 라이브러리의
     * 원본 예외 메시지가 응답으로 새어나가지 않도록 하기 위함이다.
     */
    @ExceptionHandler(RegistrationException.class)
    public ProblemDetail handleRegistration(RegistrationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("패스키 등록 실패");
        if (ex.getCause() != null) {
            log.warn("패스키 등록 실패", ex);
        }
        return problemDetail;
    }
}
