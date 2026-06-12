# Passkey Demo Project Guidelines (프로젝트 지침)

## Language (언어)
- 모든 답변과 개발 관련 설명은 반드시 **한글(한국어)**로 작성해 주세요. (Always respond in Korean.)

## Command Reference (명령어 참조)
- **백엔드 컴파일**: `./gradlew compileJava`
- **백엔드 빌드**: `./gradlew build`
- **백엔드 테스트**: `./gradlew test`
- **프론트엔드 빌드**: `pnpm --filter frontend build` (혹은 `frontend` 디렉토리 내에서 `pnpm build`)

## Architecture & Code Rules (아키텍처 및 코드 규칙)
- **비로그인 사용자 가입**: 기본 Spring Security WebAuthn 필터의 제한을 우회하기 위해 가입 관련 API는 `/api/register/options` 및 `/api/register` 커스텀 컨트롤러(`WebAuthnRegistrationController`)를 통해 처리합니다.
- **로그인(인증)**: 스프링 시큐리티의 기본 엔드포인트(`/webauthn/authenticate/options` 및 `/login/webauthn`)를 그대로 활용합니다.
- **프론트엔드**: Vue 3 (Composition API / `<script setup>`) 및 Sleek Dark Mode 스타일을 사용하며, 개발 환경에서는 HTTPS(Vite mkcert) 기반으로 구동됩니다.
