# Duster browser SDK

The npm workspace for the Duster frontend SDK — a framework-agnostic core plus thin per-framework
adapters. Design: `docs/duster-v1-design.md` #11, #12, #31.

| package | status | what |
|---------|--------|------|
| [`@authoss/duster-core`](./core) | **shipped** | zero-dep client + observable store; also the `<script>` global (vanilla-JS SDK) |
| [`@authoss/duster-react`](./react) | **shipped** | `<DusterProvider>` + `useDuster()` + `<ProtectedRoute>` |
| [`@authoss/duster-vue`](./vue) | **shipped** | `createDuster()` plugin + `useDuster()` + `<ProtectedRoute>` |
| [`@authoss/duster-angular`](./angular) | **shipped** | `provideDuster()` + `DusterService` (signals) + `dusterAuthGuard` |

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
npm test            # vitest — one project per package
npm run typecheck
npm run lint
npm run sync-license # copy packages/LICENSE into each publishable package
```

`core` / `react` / `vue` share TypeScript `~5.7`. `angular` pins its own `typescript@~5.8` (its
devDep `@angular/*@20` requires `>=5.8`); the contained workspace keeps that isolated — every
package runs its own `tsc`, and the shared Vitest / esbuild transpiles all of them regardless.
The Angular adapter has no components/directives/templates, so it builds with `tsup` like the
others — no `ng-packagr` / Ivy partial-compilation step is needed.

## Licensing

The repo root is GPL-3.0 (the Authos server + CLI). **Everything under `packages/` is MIT** — a
copyleft client library that bundles into a consumer's app defeats the "delegate auth with no code"
goal. `packages/LICENSE` is canonical; `npm run sync-license` copies it into each package.

## Publishing

`.github/workflows/sdk-release.yaml` publishes all four packages **lockstep** (one version per
release) via npm **trusted publishing** — OIDC from GitHub Actions, no `NPM_TOKEN` secret, provenance
automatic.

**Cut a release:**

```bash
git tag duster-sdk-v0.2.0 && git push origin duster-sdk-v0.2.0
```

The workflow checks out the tag, runs `scripts/set-release-version.mjs` (stamps the version on
`core` / `react` / `vue` / `angular` and pins the adapters' `@authoss/duster-core` range from `*` to
`^<version>`), builds, then `npm publish`es core first, then the adapters — pausing on the `release`
environment gate for a maintainer to approve. The committed `package.json` versions stay `0.0.0` —
the release version is never committed. A re-run is safe: each package is skipped if that exact
version is already on the registry. A **dry run** (packs + validates, publishes nothing) is
`Actions → sdk-release → Run workflow`.

**One-time setup on npmjs.com — done (2026-08-29).** All four packages went live at `0.1.0` (a
manual bootstrap publish, needed before a trusted publisher can be attached to a name), and
`0.1.1` was the first CI release — published by GitHub Actions over OIDC with SLSA provenance. The
standing configuration, for reference:

- Per package (`@authoss/duster-{core,react,vue,angular}`): **Settings → Trusted publishing →
  GitHub Actions** → org/user `stevetosak`, repo `authos`, workflow `sdk-release.yaml`,
  environment `release`, allowed action `npm publish`.
- Repo **Settings → Environments → `release`** with a required reviewer, so every real publish waits
  for an approval.
- The `@authoss` org requires 2FA and disallows publish tokens — releases go through trusted
  publishing (CI) or interactive 2FA (local), never a token.

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
