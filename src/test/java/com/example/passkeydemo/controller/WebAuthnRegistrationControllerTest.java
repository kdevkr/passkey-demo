package com.example.passkeydemo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link WebAuthnRegistrationController}의 횡단 관심사 검증.
 *
 * <p>Bean Validation(@Valid) → {@code ProblemDetail} 변환과
 * Spring Framework 7 API 버저닝이 실제로 동작하는지 확인한다. 검증 실패 경로만
 * 다루므로 실제 WebAuthn 자격증명이나 DB 쓰기 없이 안전하게 테스트된다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class WebAuthnRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void optionsWithBlankUsername_returnsProblemDetail() throws Exception {
        mockMvc.perform(post("/api/register/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    void registerWithEmptyBody_returnsValidationError() throws Exception {
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void unsupportedApiVersion_isRejected() throws Exception {
        // 지원 버전은 1.0 뿐 — 2.0 요청은 핸들러 매핑 단계에서 거부되어야 한다.
        mockMvc.perform(post("/api/register/options")
                        .header("X-API-Version", "2.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"version-probe\"}"))
                .andExpect(status().is4xxClientError());
    }
}
