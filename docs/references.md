# Passkey (WebAuthn) Reference Guides

This document collects and summarizes the official documentation and integration guides for implementing Passkeys in a Spring Boot backend and Vue frontend.

---

## 1. Official Standards & Overviews

### Google Passkeys Guide
* **Link**: [Google Identity Passkeys](https://developers.google.com/identity/passkeys?hl=ko)
* **Key Concept**: 
  - Explains the user experience and protocol mechanics of Passkeys.
  - Detail how Passkeys leverage WebAuthn APIs (`navigator.credentials.create` and `navigator.credentials.get`) to create and verify cryptographic credentials.

### SKT Passkey Guide
* **Link**: [SK Telecom Passkey](https://www.passkey-sktelecom.com/)
* **Key Concept**:
  - Provides a localization and deployment reference in Korea for Passkeys.
  - Demonstrates enterprise integration design and UX best practices for Korean users.

---

## 2. Spring Boot Backend Integration

### Spring Security Passkeys (Official Support)
* **Link**: [Spring Security Passkeys Reference](https://docs.spring.io/spring-security/reference/servlet/authentication/passkeys.html)
* **Key Architecture**:
  - **Spring Security 6.4+ / 7.0** introduces native Passkey (WebAuthn) support.
  - Core component: `PasskeyAuthenticationFilter` handles incoming login request authentication.
  - Relying Party (RP) configuration: `RelyingPartyRegistration` defines the RP ID, RP Name, and allowed algorithms.
  - User credentials are stored via `PasskeyUserCredentialsService`.

### WebAuthn4J Spring Security
* **Link**: [WebAuthn4J Spring Security](https://webauthn4j.github.io/webauthn4j-spring-security/en/)
* **Key Architecture**:
  - A popular library used for Spring Security integration before native support was introduced, and still widely used for advanced configurations.
  - Offers custom entities, verification logic, and converters.

### Baeldung Integration Guide
* **Link**: [Integrating Passkeys into Spring Security | Baeldung](https://www.baeldung.com/spring-security-integrate-passkeys)
* **Practical Code Example**:
  - Shows how to configure Spring Security filter chains for Passkey authentication.
  - Details custom JPA entities to store user credentials (credential ID, public key, signature counter).
  - Outlines the step-by-step controller setup to supply options (`PublicKeyCredentialCreationOptions` and `PublicKeyCredentialRequestOptions`) to the frontend.

---

## 3. Frontend WebAuthn Library

### SimpleWebAuthn
* **Link**: [SimpleWebAuthn Documentation](https://simplewebauthn.dev/)
* **Key Concept**:
  - The standard library for WebAuthn in JavaScript.
  - `@simplewebauthn/browser` provides wrapper functions `startRegistration()` and `startAuthentication()` which handle all base64url encoding/decoding, browser compatibility checks, and calling the native `navigator.credentials` APIs.
  - Using this avoids manually parsing binary buffers to and from the server.

---

## 3. Frontend Integration Flow

When the frontend communicates with the backend, the flow looks like:

### A. Registration Flow (SignUp)
```
[Vue Frontend]                                [Spring Boot Backend]
      |                                                |
      | ---- 1. GET /api/passkey/register/options ---> |
      | <--- 2. Return creation options (JSON) ------- |
      |                                                |
      | -- 3. Call navigator.credentials.create() ---> |
      | <--- 4. Receive newly created credential ----- |
      |                                                |
      | ---- 5. POST /api/passkey/register/finish ---> |
      | <--- 6. Return registration status ----------- |
```

### B. Authentication Flow (SignIn)
```
[Vue Frontend]                                [Spring Boot Backend]
      |                                                |
      | ---- 1. GET /api/passkey/login/options ------> |
      | <--- 2. Return assertion options (JSON) ------ |
      |                                                |
      | -- 3. Call navigator.credentials.get() ------> |
      | <--- 4. Receive credential assertion --------- |
      |                                                |
      | ---- 5. POST /login (or /api/login/finish) --> |
      | <--- 6. Authentication Successful ------------ |
```
