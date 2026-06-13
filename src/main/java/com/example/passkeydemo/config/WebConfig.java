package com.example.passkeydemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정.
 *
 * <p>Spring Framework 7에 내장된 API 버저닝을 활성화한다. 버전은
 * {@code X-API-Version} 요청 헤더로 전달하며, 핸들러는
 * {@code @RequestMapping(version = "...")}로 자신이 처리하는 버전을 선언한다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                // 버전을 X-API-Version 헤더에서 읽는다 (URL은 깨끗하게 유지)
                .useRequestHeader("X-API-Version")
                // 서버가 지원하는 버전 목록
                .addSupportedVersions("1.0")
                // 헤더가 없는 요청은 1.0으로 간주 → 기존 프론트엔드 하위 호환
                .setDefaultVersion("1.0");
    }
}
