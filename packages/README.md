# Duster browser SDK

The npm workspace for the Duster frontend SDK — a framework-agnostic core plus thin per-framework
adapters. Design: `docs/duster-v1-design.md` #11, #12, #31.

| package | status | what |
|---------|--------|------|
| [`@authoss/duster-core`](./core) | **shipped** | zero-dep client + observable store; also the `<script>` global (vanilla-JS SDK) |
| [`@authoss/duster-react`](./react) | **shipped** | `<DusterProvider>` + `useDuster()` + `<ProtectedRoute>` |
| `@authoss/duster-vue` | later | `createDuster()` plugin + `useDuster()` composable |
| `@authoss/duster-angular` | later | `provideDuster()` + `DusterService` + `dusterAuthGuard` |

Not published: [`examples/react-vite`](./examples/react-vite) (a tier-0 SPA that consumes
`@authoss/duster-react`) and [`e2e`](./e2e) (a Playwright suite that drives that SPA through
login / silent refresh / logout against `e2e-tests/docker-compose.e2e.yml`).

## Layout

This is a **contained** npm workspace — it is not part of the Gradle build and not a repo-root
workspace. Run everything from `packages/`:

```bash
cd packages
npm install
npm run build       # sequences core → adapters (npm --workspaces run is not topological)
npm test            # vitest
npm run typecheck
npm run lint
npm run sync-license # copy packages/LICENSE into each publishable package
```

## Licensing

The repo root is GPL-3.0 (the Authos server + CLI). **Everything under `packages/` is MIT** — a
copyleft client library that bundles into a consumer's app defeats the "delegate auth with no code"
goal. `packages/LICENSE` is canonical; `npm run sync-license` copies it into each package.

## Publishing

Deferred until all adapters land. When ready: **npm trusted publishers** (OIDC from GitHub Actions,
no `NPM_TOKEN`). Tag `duster-sdk-vX.Y.Z` → `.github/workflows/sdk-release.yaml` publishes all
packages lockstep. Each `@authoss/duster-*` needs a one-time trusted-publisher config on npmjs.com
(org `stevetosak`, repo `authos`, workflow `sdk-release.yaml`).

## `npm pack` smoke test

```bash
cd packages && npm run build
npm pack -w @authoss/duster-core
# in a fresh Vite app:
npm i ../path/to/authoss-duster-core-*.tgz
```

## Browser e2e

Closes the Phase 1 exit criterion — the real `@authoss/duster-react` package, consumed by a real
Vite SPA, driven from a browser.

```bash
cd packages && npm install && npm run build
docker compose -p authos-sdk-e2e -f ../e2e-tests/docker-compose.e2e.yml up -d   # needs the jars + keystore:
#   ../gradlew :duster:buildFatJar :e2e-tests:buildAuthosApiJar :e2e-tests:generateTestKeystore
npx playwright install chromium
npm run e2e                                                                     # seeds, then runs Playwright
docker compose -p authos-sdk-e2e -f ../e2e-tests/docker-compose.e2e.yml down -v
```

CI runs this as the `browser-e2e` job in `.github/workflows/sdk.yaml`.
