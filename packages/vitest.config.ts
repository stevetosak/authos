import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitest/config'

// Resolve the workspace package from source so tests don't need a build.
const coreSrc = fileURLToPath(new URL('./core/src/index.ts', import.meta.url))

export default defineConfig({
  resolve: {
    alias: { '@authoss/duster-core': coreSrc },
  },
  test: {
    projects: [
      {
        resolve: { alias: { '@authoss/duster-core': coreSrc } },
        test: {
          name: 'core',
          root: './core',
          environment: 'node',
          include: ['test/**/*.test.ts'],
        },
      },
      {
        resolve: { alias: { '@authoss/duster-core': coreSrc } },
        test: {
          name: 'react',
          root: './react',
          environment: 'jsdom',
          include: ['test/**/*.test.{ts,tsx}'],
          setupFiles: ['./test/setup.ts'],
        },
      },
      {
        resolve: { alias: { '@authoss/duster-core': coreSrc } },
        test: {
          name: 'vue',
          root: './vue',
          environment: 'jsdom',
          include: ['test/**/*.test.ts'],
          setupFiles: ['./test/setup.ts'],
        },
      },
      {
        resolve: { alias: { '@authoss/duster-core': coreSrc } },
        test: {
          name: 'angular',
          root: './angular',
          environment: 'jsdom',
          include: ['test/**/*.test.ts'],
          setupFiles: ['./test/setup.ts'],
        },
      },
    ],
    coverage: {
      provider: 'v8',
      include: ['*/src/**/*.{ts,tsx}'],
      exclude: ['*/src/**/*.d.ts', '*/src/global.ts', '*/src/types.ts', '*/src/context.ts'],
      reporter: ['text', 'lcov'],
    },
  },
})
