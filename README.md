# Spring Boot 4 + Vue 3 Passkey Demo

Spring Boot 4의 Spring Security WebAuthn과 Vue 3를 연동한 패스키 로그인/회원가입 데모입니다.

## 🛠️ 기술 스택 (Tech Stack)

### Backend
- **Java 21**
- **Spring Boot 4.1.0**
- **Spring Security 7** (Native WebAuthn)
- **Gradle** (Kotlin DSL)

### Frontend
- **Vue 3**
- **Vite**
- **@simplewebauthn/browser**
- **pnpm**

## 🚀 실행 방법

### 1. 백엔드 실행 (localhost:5000)
```bash
./gradlew bootRun
```

### 2. 프론트엔드 실행 (localhost:8080)
```bash
cd frontend
pnpm install
pnpm dev
```

> 🔐 **HTTPS 설정 (mkcert)**: 패스키 인증(WebAuthn)은 브라우저 보안 제약으로 인해 로컬 개발 환경에서도 HTTPS가 필수입니다.
> 1. `mkcert -install` 명령어로 로컬 루트 CA를 신뢰할 수 있는 기관에 추가합니다.
> 2. `mkcert localhost` 명령어로 `localhost`용 SSL 인증서를 발급합니다.
> 3. 발급받은 인증서 파일을 프론트엔드(Vite) 및 백엔드(Spring Boot Keystore)에 등록하여 HTTPS 서버를 가동합니다.
