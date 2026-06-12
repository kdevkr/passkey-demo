<template>
  <div class="passkey-container">
    <div class="glow-bg"></div>
    <div class="passkey-card">
      <div class="card-header">
        <div class="security-icon">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 5.25a3 3 0 0 1 3 3m3 0a6 6 0 0 1-7.029 5.912c-.563-.097-1.159.026-1.563.43L10.5 17.25H8.25v2.25H6v2.25H2.25v-2.818c0-.597.237-1.17.659-1.591l6.499-6.499c.404-.404.527-1 .43-1.563A6 6 0 1 1 21.75 8.25Z" />
          </svg>
        </div>
        <h1>Passkey Demo</h1>
        <p class="subtitle">비밀번호 없는 안전한 로그인을 경험해보세요.</p>
      </div>

      <div class="card-body">
        <!-- 등록 섹션 -->
        <div class="section register-section">
          <h2>패스키 등록 (회원가입)</h2>
          <div class="input-group">
            <input 
              v-model="username" 
              type="text" 
              placeholder="이메일 또는 아이디 입력" 
              :disabled="loading"
              @keyup.enter="handleRegister"
            />
          </div>
          <button 
            class="btn btn-primary" 
            :disabled="loading || !username.trim()"
            @click="handleRegister"
          >
            <span v-if="loading && action === 'register'" class="spinner"></span>
            <span>패스키 등록</span>
          </button>
        </div>

        <div class="divider">
          <span>또는</span>
        </div>

        <!-- 로그인 섹션 -->
        <div class="section login-section">
          <button 
            class="btn btn-secondary" 
            :disabled="loading"
            @click="handleLogin"
          >
            <span v-if="loading && action === 'login'" class="spinner"></span>
            <span>패스키로 로그인</span>
          </button>
        </div>
      </div>

      <!-- 모킹 토글 스위치 -->
      <div class="mock-toggle-container">
        <label class="switch">
          <input type="checkbox" v-model="mockMode" />
          <span class="slider round"></span>
        </label>
        <span class="mock-label">
          백엔드 모킹 모드 
          <span class="mock-badge" :class="{ 'badge-active': mockMode }">
            {{ mockMode ? 'MOCK ON' : 'OFF' }}
          </span>
        </span>
      </div>

      <!-- 상태 메시지 배너 -->
      <transition name="fade">
        <div v-if="statusMessage" :class="['status-banner', statusType]">
          <span class="status-icon">
            <svg v-if="statusType === 'success'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
            </svg>
            <svg v-else-if="statusType === 'error'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="m9.75 9.75 4.5 4.5m0-4.5-4.5 4.5M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
            </svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9.879 7.519c1.171-1.025 3.071-1.025 4.242 0 1.172 1.025 1.172 2.687 0 3.712-.203.179-.43.326-.67.442-.745.361-1.45.999-1.45 1.827v.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9 5.25h.008v.008H12v-.008Z" />
            </svg>
          </span>
          <span class="status-text">{{ statusMessage }}</span>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { startRegistration, startAuthentication } from '@simplewebauthn/browser'

const username = ref('')
const loading = ref(false)
const action = ref('') // 'register' | 'login'
const statusMessage = ref('')
const statusType = ref('info') // 'info' | 'success' | 'error'
const mockMode = ref(true) // 기본적으로 모킹 모드를 활성화해두어 단독 테스트가 쉽게 구성

const setStatus = (msg, type = 'info') => {
  statusMessage.value = msg
  statusType.value = type
}

// Mocking options and helpers
const getMockRegisterOptions = (name) => {
  return {
    challenge: 'MTIzNDU2Nzg5MDEyMzQ1Ng', // Base64URL challenge
    rp: {
      name: 'Passkey Demo (Mock)',
      id: window.location.hostname
    },
    user: {
      id: 'dXNlci1pZC0xMjM0NTY', // Base64URL user ID
      name: name,
      displayName: name
    },
    pubKeyCredParams: [
      { alg: -7, type: 'public-key' },  // ES256
      { alg: -257, type: 'public-key' } // RS256
    ],
    timeout: 60000,
    attestation: 'none',
    excludeCredentials: [],
    authenticatorSelection: {
      residentKey: 'preferred',
      requireResidentKey: false,
      userVerification: 'preferred'
    }
  }
}

const getMockLoginOptions = () => {
  const creds = JSON.parse(localStorage.getItem('mock_credentials') || '[]')
  const allowCredentials = creds.map(c => ({
    id: c.credentialId,
    type: 'public-key'
  }))

  return {
    challenge: 'MTIzNDU2Nzg5MDEyMzQ1Ng',
    rpId: window.location.hostname,
    allowCredentials: allowCredentials,
    userVerification: 'preferred'
  }
}

const handleRegister = async () => {
  if (!username.value.trim()) return
  loading.value = true
  action.value = 'register'
  setStatus('등록 정보를 요청하고 있습니다...', 'info')

  try {
    let options
    if (mockMode.value) {
      // Mock Options
      options = getMockRegisterOptions(username.value.trim())
    } else {
      // Real API Options
      const optionsRes = await fetch(`/api/passkey/register/options?username=${encodeURIComponent(username.value.trim())}`)
      if (!optionsRes.ok) {
        throw new Error('등록 옵션을 가져오는 데 실패했습니다.')
      }
      options = await optionsRes.json()
    }

    // Start browser WebAuthn registration
    setStatus('기기 인증을 진행 중입니다...', 'info')
    const credential = await startRegistration({ optionsJSON: options })

    setStatus('등록 완료를 검증하는 중입니다...', 'info')
    
    if (mockMode.value) {
      // Mock verification and save credential in LocalStorage
      const creds = JSON.parse(localStorage.getItem('mock_credentials') || '[]')
      creds.push({
        username: username.value.trim(),
        credentialId: credential.id,
        rawId: credential.rawId
      })
      localStorage.setItem('mock_credentials', JSON.stringify(creds))

      setStatus(`패스키 등록이 완료되었습니다! (MOCK - 계정: ${username.value.trim()})`, 'success')
      username.value = ''
    } else {
      // Real API verification
      const finishRes = await fetch('/api/passkey/register/finish', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(credential)
      })

      if (finishRes.ok) {
        setStatus('패스키 등록이 완료되었습니다!', 'success')
        username.value = ''
      } else {
        const errText = await finishRes.text()
        throw new Error(errText || '패스키 등록 완료 검증에 실패했습니다.')
      }
    }
  } catch (error) {
    console.error(error)
    setStatus(error.message || '패스키 등록 중 오류가 발생했습니다.', 'error')
  } finally {
    loading.value = false
    action.value = ''
  }
}

const handleLogin = async () => {
  loading.value = true
  action.value = 'login'
  setStatus('로그인 정보를 요청하고 있습니다...', 'info')

  try {
    let options
    if (mockMode.value) {
      options = getMockLoginOptions()
    } else {
      const optionsRes = await fetch('/api/passkey/login/options')
      if (!optionsRes.ok) {
        throw new Error('로그인 옵션을 가져오는 데 실패했습니다.')
      }
      options = await optionsRes.json()
    }

    // Start browser WebAuthn authentication
    setStatus('기기 인증을 진행 중입니다...', 'info')
    const assertion = await startAuthentication({ optionsJSON: options })

    setStatus('로그인 완료를 검증하는 중입니다...', 'info')

    if (mockMode.value) {
      // Mock verification
      const creds = JSON.parse(localStorage.getItem('mock_credentials') || '[]')
      const match = creds.find(c => c.credentialId === assertion.id)
      if (!match) {
        throw new Error('등록되지 않은 패스키 기기입니다. 먼저 등록을 완료해 주세요.')
      }

      setStatus(`성공적으로 로그인되었습니다! (MOCK - 계정: ${match.username})`, 'success')
    } else {
      // Real API verification
      const finishRes = await fetch('/api/passkey/login/finish', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(assertion)
      })

      if (finishRes.ok) {
        setStatus('성공적으로 로그인되었습니다!', 'success')
      } else {
        const errText = await finishRes.text()
        throw new Error(errText || '로그인 검증에 실패했습니다.')
      }
    }
  } catch (error) {
    console.error(error)
    setStatus(error.message || '로그인 중 오류가 발생했습니다.', 'error')
  } finally {
    loading.value = false
    action.value = ''
  }
}
</script>

<style scoped>
.passkey-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  position: relative;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
  color: #f3f4f6;
  overflow: hidden;
}

.glow-bg {
  position: absolute;
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(79, 70, 229, 0.35) 0%, rgba(6, 182, 212, 0.1) 70%, transparent 100%);
  filter: blur(50px);
  z-index: 0;
  pointer-events: none;
}

.passkey-card {
  width: 100%;
  max-width: 440px;
  background: rgba(17, 18, 23, 0.7);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.card-header {
  text-align: center;
}

.security-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.15), rgba(6, 182, 212, 0.15));
  border: 1px solid rgba(79, 70, 229, 0.3);
  border-radius: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 16px auto;
  color: #06b6d4;
}

.security-icon svg {
  width: 28px;
  height: 28px;
}

h1 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #ffffff 50%, #a5f3fc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  font-size: 14px;
  color: #9ca3af;
  margin: 0;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

h2 {
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  color: #9ca3af;
  margin: 0;
}

.input-group input {
  width: 100%;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 14px 16px;
  color: #ffffff;
  font-size: 15px;
  transition: all 0.2s ease-in-out;
  box-sizing: border-box;
}

.input-group input:focus {
  outline: none;
  border-color: #4f46e5;
  background: rgba(79, 70, 229, 0.03);
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.15);
}

.btn {
  width: 100%;
  padding: 14px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease-in-out;
  box-sizing: border-box;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, #4f46e5, #06b6d4);
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.95;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.35);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #f3f4f6;
}

.btn-secondary:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.divider {
  display: flex;
  align-items: center;
  text-align: center;
  color: #4b5563;
  font-size: 12px;
  font-weight: 500;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.divider::before {
  margin-right: .5em;
}

.divider::after {
  margin-left: .5em;
}

/* Mocking Toggle Styling */
.mock-toggle-container {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px dashed rgba(255, 255, 255, 0.08);
  border-radius: 14px;
}

.mock-label {
  font-size: 13.5px;
  color: #9ca3af;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.mock-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 6px;
  background: #374151;
  color: #9ca3af;
}

.badge-active {
  background: rgba(6, 182, 212, 0.2);
  color: #22d3ee;
}

.switch {
  position: relative;
  display: inline-block;
  width: 38px;
  height: 22px;
  flex-shrink: 0;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.1);
  transition: .3s;
}

.slider:before {
  position: absolute;
  content: "";
  height: 16px;
  width: 16px;
  left: 3px;
  bottom: 3px;
  background-color: #d1d5db;
  transition: .3s;
}

input:checked + .slider {
  background-color: #06b6d4;
}

input:checked + .slider:before {
  transform: translateX(16px);
  background-color: #ffffff;
}

.slider.round {
  border-radius: 22px;
}

.slider.round:before {
  border-radius: 50%;
}

.status-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 13.5px;
  line-height: 1.4;
}

.status-icon {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.status-icon svg {
  width: 18px;
  height: 18px;
}

.info {
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.2);
  color: #93c5fd;
}

.success {
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.2);
  color: #6ee7b7;
}

.error {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: #fca5a5;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #ffffff;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
