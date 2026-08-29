import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    environment: 'node',
    include: ['*/test/**/*.test.ts'],
    coverage: {
      provider: 'v8',
      include: ['*/src/**/*.ts'],
      exclude: ['*/src/**/*.d.ts', '*/src/global.ts', '*/src/types.ts'],
      reporter: ['text', 'lcov'],
    },
  },
})
