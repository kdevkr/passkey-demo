# Spec: Passkey Registration & Login UI

* **Date**: 2026-06-12
* **Status**: Approved

This specification defines the frontend UI and client-side logic for Passkey registration and login.

---

## 1. User Interface (UI) Design

### Component
A single card component located in `frontend/src/components/PasskeyAuth.vue` (and imported in `frontend/src/App.vue`).

### Visual Style
- Premium dark card layout with smooth shadows and micro-interactions.
- Focused state highlights and alert banners for status feedback.

### Key Layout Elements
1. **Title**: "Passkey Demo"
2. **Registration Area**:
   - Username/Email text input.
   - "Register Passkey" button.
3. **Divider**: "or"
4. **Authentication Area**:
   - "Sign In with Passkey" button.
5. **Status Area**:
   - Visual alert indicating Loading, Success, or Error messages.

---

## 2. Technical Flow & Integration

We will use `@simplewebauthn/browser` helper functions to interface with the WebAuthn API.

### A. Registration (Register Passkey)
1. **Get Options**: `GET /api/passkey/register/options?username=<username>`
2. **Create Credential**:
   ```javascript
   import { startRegistration } from '@simplewebauthn/browser';
   const regResult = await startRegistration(optionsJSON);
   ```
3. **Verify finish**: `POST /api/passkey/register/finish` with `regResult` body.

### B. Authentication (Login)
1. **Get Options**: `GET /api/passkey/login/options`
2. **Get Assertion**:
   ```javascript
   import { startAuthentication } from '@simplewebauthn/browser';
   const authResult = await startAuthentication(optionsJSON);
   ```
3. **Verify finish**: `POST /api/passkey/login/finish` with `authResult` body.
