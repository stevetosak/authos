import { fileURLToPath } from 'node:url'
import { defineConfig, devices } from '@playwright/test'

const EXAMPLE_DIR = fileURLToPath(new URL('../examples/react-vite', import.meta.url))

/** The example SPA must serve on 5173 — the compose stack's Authos is configured with
 *  `FRONTEND_HOST=http://localhost:5173` and 302s the browser there during `/oauth/authorize`. */
const BASE_URL = 'http://localhost:5173'

export default defineConfig({
  testDir: './specs',
  fullyParallel: false,
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: 0,
  reporter: process.env.CI ? [['github'], ['html', { open: 'never' }]] : [['list'], ['html', { open: 'never' }]],
  timeout: 90_000,
  globalSetup: './global-setup.ts',
  use: {
    baseURL: BASE_URL,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: 'npm run build && npm run preview',
    cwd: EXAMPLE_DIR,
    url: BASE_URL,
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
    stdout: 'pipe',
    stderr: 'pipe',
  },
})
