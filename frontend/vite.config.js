import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import fs from 'fs'
import path from 'path'

const certDir = path.resolve(__dirname, 'cert')
let certPath = null
let keyPath = null
let useTailscale = false

if (fs.existsSync(certDir)) {
  const files = fs.readdirSync(certDir)
  const certFile = files.find(f => f.endsWith('.ts.net.crt'))
  const keyFile = files.find(f => f.endsWith('.ts.net.key'))

  certPath = certFile ? path.resolve(certDir, certFile) : null
  keyPath = keyFile ? path.resolve(certDir, keyFile) : null
  useTailscale = certPath && keyPath && fs.existsSync(certPath) && fs.existsSync(keyPath)
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 8080,
    host: true, // Listen on all local IPs including Tailscale IP
    https: useTailscale ? {
      key: fs.readFileSync(keyPath),
      cert: fs.readFileSync(certPath),
    } : true, // If tailscale cert is not ready, default to standard https fallback (vite-plugin-mkcert can be removed, Vite 5+ has basic built-in https options or we can fallback to HTTP/basic https)
    proxy: {
      '/api': {
        target: 'http://localhost:5000',
        changeOrigin: true,
        secure: false,
      },
      '/webauthn': {
        target: 'http://localhost:5000',
        changeOrigin: true,
        secure: false,
      },
      '/login': {
        target: 'http://localhost:5000',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})
