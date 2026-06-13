# Spring Boot 4 + Vue 3 Passkey Demo

Spring Boot 4의 Spring Security WebAuthn과 Vue 3를 연동한 패스키 로그인/회원가입 데모입니다.

## 🛠️ 기술 스택 (Tech Stack)

### Backend
- **Java 25**
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

> 🔐 **HTTPS 설정**: 패스키 인증(WebAuthn)은 브라우저 보안 제약으로 인해 로컬 개발 환경에서도 HTTPS가 필수입니다.
> 
> **방법 1) mkcert를 통한 로컬 HTTPS 구성**
> 1. `mkcert -install` 명령어로 로컬 루트 CA를 신뢰할 수 있는 기관에 추가합니다.
> 2. `mkcert localhost` 명령어로 `localhost`용 SSL 인증서를 발급합니다.
> 3. 발급받은 인증서 파일을 등록하여 HTTPS 서버를 가동합니다.
> 
> **방법 2) Tailscale을 통한 HTTPS 구성 (외부 모바일 기기 테스트 등)**
> 1. [Tailscale Admin Console Settings](https://login.tailscale.com/admin/settings/dns)에서 **MagicDNS** 및 **HTTPS Certificates**를 활성화합니다.
> 2. `frontend` 디렉토리로 이동한 후 아래 명령어로 Let's Encrypt 실인증서를 발급받아 `cert` 폴더에 위치시킵니다.
>    ```bash
>    cd frontend
>    mkdir cert
>    tailscale cert [내-기기-이름].[내-도메인].ts.net
>    mv *.ts.net.crt cert/
>    mv *.ts.net.key cert/
>    ```
>    *Vite 설정(`vite.config.js`)이 `cert/` 폴더 내의 Tailscale 인증서를 자동으로 감지하여 HTTPS로 로드합니다.*

## 🔗 패스키 데모 체험 사이트
- [WebAuthn.io](https://webauthn.io/)
- [SKT Passkey 체험하기](https://www.passkey-sktelecom.com/experience)
- [Google Passkeys Demo](https://passkeys-demo.appspot.com/)
- [드림시큐리티 패스키 솔루션](https://www.dreamsecurity.com/solution/certification/passkey)

