import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Tier-0 setup: the SPA is served from its own origin and `/duster` is reverse-proxied onto it,
// so from the browser's point of view Duster lives at the same origin (no CORS, `SameSite=Lax`
// cookie). `vite preview` runs on 5173 to match the compose stack's `FRONTEND_HOST` — the Authos
// authorize step 302s the browser to `<FRONTEND_HOST>/oauth/login`, which the e2e reads params off.
const DUSTER_TARGET = process.env.DUSTER_TARGET ?? 'http://localhost:18785'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: { '/duster': { target: DUSTER_TARGET } },
  },
  preview: {
    port: 5173,
    strictPort: true,
    proxy: { '/duster': { target: DUSTER_TARGET } },
  },
})
