# Authos — Monorepo CLAUDE.md

## What Is This

Authos is a self-hosted OpenID Connect (OIDC) Identity Provider ecosystem. The JVM modules live here as a Gradle multi-project build; `authos-frontend/`, `authos-demo/`, and `packages/` are standalone npm projects.

```
authos-api/       Spring Boot OIDC IDP (the auth server)
authos-frontend/  React SPA (login, consent, dashboard UI)
authos-demo/      React SPA — public guided walkthrough of a real tier-0 login (authos-demo.tosak.net)
duster/           Ktor BFF proxy (OAuth flow handler for client apps)
dstr-cli/         Kotlin CLI (manage Duster from the terminal)
e2e-tests/        Kotlin/JUnit HTTP-level suite — spins up the whole stack via docker-compose
packages/         Duster browser SDK — contained npm workspace: @authoss/duster-core (+ React/Vue/Angular adapters)
```

Roadmap & status: `docs/roadmap.md` (plan), `docs/duster-v1-tasks.md` (checklist),
`docs/CHANGELOG.md` (append-only history), `docs/duster-v1-design.md` (numbered decisions).
Editing rules for these are in `docs/README.md`.

## System Map

```
Browser / Client App
  │
  ├─→ authos-frontend (:5173 dev / nginx prod)  ← login, consent, admin UI
  │       │
  │       └─→ authos-api (:8080)                ← OIDC IDP (Spring Boot)
  │                 │
  │                 └─→ PostgreSQL + Redis
  │
  └─→ duster (:8785)                            ← BFF proxy (Ktor)
          │   stores tokens in Redis
          └─→ authos-api (:8080)                ← token exchange, JWKS, userinfo

dstr (CLI) → duster + authos-api               ← app registration / sync
```

## Modules

### authos-api — OIDC Identity Provider
- **Stack:** Spring Boot 3, Kotlin/Java, PostgreSQL (Flyway), Redis, Nimbus JOSE+JWT
- **Build:** `./mvnw clean package` / `./mvnw spring-boot:run`
- **Key concepts:**
  - `AppGroup` — groups apps; defines SSO and MFA policy
  - `PPID` — pairwise pseudonymous `sub` per group (`SHA-256(groupId+userId+salt)`)
  - `ShortSession` — Redis entry bridging authorize → login → consent steps
  - `SSOSession` — Redis session tracking auth state and active app
- **Flow:** `/oauth/authorize` → login/consent → `/oauth/approve` → `/oauth/token` → `/oauth/userinfo`; also `/oauth/revoke` (RFC 7009), `/.well-known/openid-configuration`. `prompt=none` mints the code straight from the SSO session.
- **JWT:** RS256 via PKCS12 keystore; pluggable `JwtTokenStrategy` per token type
- **Config:** `.env` file (dotenv-java) → `application.properties`
- **Full details:** `authos-api/CLAUDE.md`

### authos-frontend — Admin & OAuth UI
- **Stack:** React 19, TypeScript, Vite, Tailwind 4, shadcn/ui (Radix), React Router v7
- **Build:** `npm run dev` (port 5173) / `npm run build` / `npm run lint`
- **Key concepts:**
  - `AuthContext` — central auth state, verifies JWT on load and every 5 min
  - `ProtectedRoute` — gates authenticated pages
  - Axios instance in `src/services/netconfig.ts` — all API calls go here
  - `@` alias → `./src`
- **Full details:** `authos-frontend/CLAUDE.md`

### authos-demo — Public Guided Walkthrough
- **Stack:** React 19, TypeScript, Vite 7 — standalone npm project, consumes the **published** `@authoss/duster-react`
- **Build:** `npm run dev` (port 5175) / `npm run build` / `npm run typecheck` / `npm run lint`
- **What:** "The Handshake Trace" — a real tier-0 OIDC login drawn as a sequence diagram on a time base, at `authos-demo.tosak.net`. Everything shown on the wire is genuine; server-to-server hops are labelled *narrated* + linked to the `packages/e2e` CI proof.
- **Runtime config:** `client_id` from `scripts/bootstrap.ts` (one-time), injected by `entrypoint.sh` → `config/config.js` — never baked into the image.
- **Full details:** `authos-demo/CLAUDE.md`

### duster — BFF Proxy
- **Stack:** Ktor 3.1.3 (Netty), Kotlin, Koin 4.1.0, Lettuce/Redis
- **Port:** 8785
- **Build:** `./gradlew run` / `./gradlew buildFatJar` / `./gradlew buildImage`
- **Key env vars:**
  - `AUTHOS_BASE_URL` — base URL of the Authos IDP (falls back to `http://<HOST_IP>:8080`)
  - `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`, `REDIS_USESSL`
  - `HOST_IP` — rewrites `localhost` callback URIs in Docker/VM environments
- **Flow:** `/oauth/start` → Authos authorize → `/callback` (verify JWT, store tokens+session, set `duster_session_<clientId>` cookie) → redirect to `success_url`; failures redirect to `error_url`, not 500
- **Session read:** `GET /me` (browser) / `GET /session` (server-to-server) — same handler, silent-refreshes on each call
- **Logout:** `GET`/`POST /logout` — revokes the grant at Authos + purges token keys + drops session. Tier-1 apps need `POST` + `X-Duster-Csrf`
- **Tiers:** 0 (same-origin proxy), 1 (cross-origin — per-app `allowed_origins` → CORS + `SameSite=None`), 2 (backend BFF). See `duster/CLAUDE.md`
- **Token refresh:** `DusterRequestService.tryAccessTokenExchange()` → Redis lookup (`duster:token:<clientId>:<sub>:<type>`) → refresh on miss
- **Health:** `GET /health` — pings Redis; used by k8s liveness/readiness probes
- **Deployment:** `docker-compose.yml` (bundled Redis), `docker-compose.external-redis.yml`, `docker-compose.prod.yml`, `k8s/` (Kustomize base + `overlays/example`), `DEPLOY.md`. In prod: `authos` namespace, shared cluster Redis, `AUTHOS_BASE_URL=https://authos-api.tosak.net`; CI = `duster.yaml` → `stevetosak/authos-duster` image → infra-repo overlay → ArgoCD.
- **Full details:** `duster/CLAUDE.md`

### dstr-cli — Management CLI
- **Stack:** Kotlin, Clikt 5.0.1 (`SuspendingCliktCommand`), Ktor HttpClient, Mordant 3.0.0
- **Build:** `./gradlew distZip` — install via `install.sh` → binary at `~/.local/bin/dstr`
- **Command tree:**
  ```
  dstr [--host URL] [--authos-host URL]
  ├── apps [-cid | -n]       list or look up apps (Mordant table/card output)
  │   └── configure -cid     --success-url / --logout-url / --error-url / --webhook-secret
  │                          / --session-ttl / --allowed-origins a,b
  ├── sync [-cid | -n]       pull app config from Authos → save to Duster
  └── credentials            show stored credentials
      └── save -cid -cs      save client credentials to Duster
  ```
- **Config priority:** CLI flags > env vars (`DUSTER_BASE_URL`, `AUTHOS_BASE_URL`) > `~/.dstr/dstr.config` > defaults
- **Full details:** `dstr-cli/CLAUDE.md`

## CI / GitHub Actions

| Workflow | File | Trigger | Notes |
|----------|------|---------|-------|
| e2e      | `.github/workflows/e2e.yaml`      | `pull_request` touching `authos-api/**`, `duster/**`, `e2e-tests/**`; `workflow_call` | Brings the docker-compose stack up, runs `:e2e-tests:e2eTest`. No bare `push` trigger. |
| Backend  | `.github/workflows/backend.yaml`  | push to `master` (authos-api changes) | Deploy gated on the `e2e` job (`uses: ./.github/workflows/e2e.yaml`) |
| Frontend | `.github/workflows/frontend.yaml` | push to `master` (authos-frontend changes) | Deploy gated on the `e2e` job |
| Duster   | `.github/workflows/duster.yaml`   | push to `master` (duster changes) | Deploy gated on the `e2e` job. Dockerfile self-builds (no host Gradle). Image `stevetosak/authos-duster`; bumps `INFRA_REPO_DUSTER_OVERLAY_DIR`. Runs in the `authos` namespace, shares the cluster Redis. |
| Demo     | `.github/workflows/demo.yaml`     | push to `master` (authos-demo changes) | Deploy gated on the `e2e` job. Node 22 build (resolves `@authoss/duster-react` from npm), image `stevetosak/authos-demo`; bumps `INFRA_REPO_DEMO_OVERLAY_DIR`. Runs in the `authos` namespace; Ingress `/duster` path-routes to the in-cluster `duster` Service. |
| docs     | `.github/workflows/docs.yaml`     | `pull_request` touching `docs/**` | Runs `.github/scripts/doc-size.sh` — fails if a tracking doc is over its line limit |
| sdk      | `.github/workflows/sdk.yaml`      | `pull_request` touching `packages/**` | `unit` (Node 20, `cd packages`: lint / typecheck / `vitest` / build / `npm pack`) + `browser-e2e` (Playwright drives `packages/examples/react-vite` through login/refresh/logout vs `docker-compose.e2e.yml`). |
| sdk-release | `.github/workflows/sdk-release.yaml` | push tag `duster-sdk-v*`; `workflow_dispatch` (dry run) | Lockstep `npm publish` of `@authoss/duster-{core,react,vue,angular}` via npm trusted publishing (OIDC, no `NPM_TOKEN`). `scripts/set-release-version.mjs` stamps the version. `environment: release`. |

Nothing ships from `master` unless the e2e stack suite is green.

**SDK build/test** lives entirely in `packages/` (its own `package.json`, `package-lock.json`, `tsconfig`, ESLint flat config, Vitest). It is not a Gradle module. `cd packages && npm ci && npm test`. Publishing: tag `duster-sdk-vX.Y.Z` → `sdk-release.yaml`; first publish needs one-time per-package trusted-publisher config on npmjs.com — see `packages/README.md`.

## Local Dev Quick Start

```bash
# 1. Authos IDP
cd authos-api && ./mvnw spring-boot:run   # needs Postgres + Redis

# 2. Frontend
cd authos-frontend && npm run dev         # http://localhost:5173

# 3. Duster (requires authos-api + Redis)
cd duster && ./gradlew run               # http://localhost:8785

# 4. dstr CLI
cd dstr-cli && ./gradlew run --args="apps"
```

Redis quick start: `docker run -d -p 6379:6379 redis:alpine`

E2E suite (owns its own stack — no local services needed):
```bash
./gradlew :e2e-tests:e2eTest                       # builds jars, brings up docker-compose, runs all
./gradlew :e2e-tests:e2eTest --tests "com.tosak.authos.e2e.PkceThroughDusterTest"
```
Redis for the compose stack is published on host `localhost:16379` (some tests probe it directly).