# Spring Boot 4 + Vue 3 Passkey (WebAuthn) Demo

이 프로젝트는 **Spring Boot 4**의 Spring Security 네이티브 WebAuthn 지원과 **Vue 3 (Vite)** 프론트엔드를 연동하여 패스키(Passkey)를 통한 회원가입(등록) 및 로그인을 처리하는 데모 애플리케이션입니다.

---

## 🏗️ 아키텍처 및 핵심 구성요소

### 1. 프론트엔드 (Vue 3 + Vite)
- **위치**: `/frontend`
- **구동 포트**: `https://localhost:8080` (WebAuthn API를 호출하기 위해 HTTPS 필수 적용)
- **주요 라이브러리**: `@simplewebauthn/browser`
- **핵심 로직**: [PasskeyAuth.vue](file:///d:/git/passkey-demo/frontend/src/components/PasskeyAuth.vue)
  - 등록(회원가입): `/api/register/options` 및 `/api/register` API 호출
  - 로그인: `/webauthn/authenticate/options` 및 `/login/webauthn` API 호출
  - Java(Jackson) 데이터 구조의 호환성 문제를 막기 위해 클라이언트 단에서 challenge 및 key 포맷팅 필터 처리 적용

### 2. 백엔드 (Spring Boot 4)
- **위치**: `/src`
- **구동 포트**: `https://localhost:5000`
- **주요 모듈**: `spring-security-webauthn`, `tools.jackson` (Jackson 3)
- **핵심 파일**:
  - [SecurityConfig.java](file:///d:/git/passkey-demo/src/main/java/com/example/passkeydemo/config/SecurityConfig.java): 네이티브 `.webAuthn(...)` DSL 설정 및 `WebauthnJacksonModule` 빈 등록, 사용자 계정 정보를 동적으로 제공하기 위한 커스텀 `UserDetailsService` 등록
  - [WebAuthnRegistrationController.java](file:///d:/git/passkey-demo/src/main/java/com/example/passkeydemo/controller/WebAuthnRegistrationController.java): 비로그인(익명) 사용자가 패스키를 자가 등록할 수 있도록 돕는 회원가입 컨트롤러

---

## 🚀 시작하기

### 사전 요구사항
* Java 21 이상
* Node.js 18 이상
* 패스키 동작을 위해 **HTTPS (Secure Context)** 환경에서의 실행이 필요합니다.

### 1. 백엔드 실행
백엔드 루트 디렉토리에서 다음 명령어를 실행하여 서버를 시작합니다.
```bash
./gradlew bootRun
```
- 서버는 `https://localhost:5000`에서 실행됩니다.

### 2. 프론트엔드 실행
`frontend` 디렉토리로 이동하여 의존성을 설치하고 개발 서버를 시작합니다.
```bash
cd frontend
npm install
npm run dev
```
- 개발 서버는 `https://localhost:8080`에서 실행됩니다.

---

## 🛠️ 주요 기술적 해결 과제 (Troubleshooting)

1. **Jackson 3 패키지 호환성 문제**:
   - Spring Boot 4.x로 마이그레이션되면서 Jackson 라이브러리가 기존 `com.fasterxml.jackson`에서 `tools.jackson`으로 변경되었습니다.
   - 이에 맞춰 역직렬화 오류를 차단하기 위해 Spring Security 7의 Jackson 3 호환 `WebauthnJacksonModule` 빈을 설정하여 `PublicKeyCredential` 바디 파싱을 정상화했습니다.

2. **동적 유저 로그인 실패 (401 Unauthorized)**:
   - 기본 Spring Security는 WebAuthn 검증 시 `UserDetailsService` 빈을 사용합니다.
   - 인메모리 유저 저장소(`MapPublicKeyCredentialUserEntityRepository`)에 등록된 가입 유저를 로그인 필터와 동기화하기 위해, 리포지토리 기반으로 `UserDetails`를 리턴하는 커스텀 `UserDetailsService` 빈을 등록하여 로그인 실패 오류를 해결했습니다.
