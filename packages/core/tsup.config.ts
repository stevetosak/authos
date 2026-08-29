import { defineConfig } from 'tsup'

export default defineConfig([
  {
    entry: { index: 'src/index.ts' },
    format: ['esm', 'cjs'],
    dts: true,
    clean: true,
    treeshake: true,
    sourcemap: true,
    target: 'es2021',
    outExtension: ({ format }) => ({ js: format === 'cjs' ? '.cjs' : '.js' }),
  },
  {
    // vanilla-JS build: <script src=".../duster.global.js"></script> → window.Duster
    entry: { duster: 'src/global.ts' },
    format: ['iife'],
    globalName: 'Duster',
    minify: true,
    sourcemap: true,
    target: 'es2019',
    dts: false,
  },
])
