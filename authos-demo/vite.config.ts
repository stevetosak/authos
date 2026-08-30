import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Tier 0: the SPA is served from its own origin and `/duster` is reverse-proxied onto it, so from
// the browser's point of view Duster is same-origin (no CORS, `SameSite=Lax` cookie). Locally that
// proxy points at the e2e docker-compose stack (`e2e-tests/docker-compose.e2e.yml`, Duster on host
// port 18785). In production the ingress does the same path-route to the in-cluster `duster` Service.
const DUSTER_TARGET = process.env.DUSTER_TARGET ?? 'http://localhost:18785'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5175,
    strictPort: true,
    proxy: { '/duster': { target: DUSTER_TARGET, changeOrigin: true } },
  },
  preview: {
    port: 5175,
    strictPort: true,
    proxy: { '/duster': { target: DUSTER_TARGET, changeOrigin: true } },
  },
})
