# Automation Test Project — Plan

**Status:** Proposed, not yet implemented.

Goal: a standalone project that proves the Authos stack (authos-api, duster,
authos-frontend, dstr-cli) works end-to-end, replacing the ad-hoc curl/browser
verification done by hand during development. This document is the task
write-up for building it, plus a record of exactly what was manually verified
in the 2026-08-28 session so none of that verification work is lost.

---

## Scope

Services under test:
- `authos-api` (Spring Boot IDP)
- `duster` (Ktor BFF proxy)
- `authos-frontend` (login/consent UI)
- `dstr-cli` (Duster's own client, exercises the internal management API)

Out of scope for v1: load/perf testing, non-happy-path fuzzing of every
endpoint. Focus on the flows below — they're the ones that have actually
broken in practice.

## Proposed structure

- New top-level module `automation-tests/` (Playwright + TypeScript), sibling
  to the other four projects.
- Two layers, not one:
  - **HTTP-level tests** (`fetch`, no browser) for `/duster/api/v1/internal/*`,
    `/duster/pull`, `/oauth/client-info`, ownership/auth-gate checks. Fast,
    should be the majority of cases.
  - **Browser E2E specs** (Playwright) for the real login → consent → approve
    → callback click-through. Slow, kept to a handful of critical-path specs.
- Attach to already-running services by default (see Learnings below for why);
  don't have the suite own process lifecycle for local dev. A CI-only fixture
  that boots disposable jar-based instances can come later if needed.
- Seed fixture: `user1@example.com` / `TestPass123!` is a working test login
  (password was reset this session — the original hash wasn't recoverable and
  this is seed/test data, not a real account).

## Test scenarios to automate

Everything below was actually exercised by hand this session (curl + a
headless browser + direct Redis/Postgres checks) while fixing the
`offline_access`/consent bugs. Each bullet is a candidate test case:

**Duster OAuth flow**
- [ ] `GET /duster/api/v1/oauth/start?client_id=...` 302s to Authos
      `/oauth/authorize` with `prompt=consent`, correct `scope`
      (`openid profile offline_access`), `response_type=code`,
      `code_challenge` + `code_challenge_method=S256`.
- [ ] Full click-through: login with real credentials → consent page renders
      the *real* client name, *real* authenticated user's email, and the
      *real* requested scopes (not hardcoded placeholders — this was a live
      bug: the consent screen always showed `"test0"` /
      `stefantoska@authos.com` regardless of who was logging in) → Allow
      Access → redirect completes with no `>=400` response and no landing on
      the frontend's generic error page.
- [ ] Negative: login form rejects bad credentials without a 5xx / stack
      trace leak.

**Duster internal management API**
- [ ] Every `/duster/api/v1/internal/*` route 401s without
      `Authorization: Bearer <DUSTER_ADMIN_TOKEN>`, and succeeds with it.
- [ ] `POST /internal/apps/create` is upsert-safe: register an app, `PATCH
      /internal/apps/config` with custom `webhookSecret`/`sessionTtl`, re-run
      the equivalent of `dstr sync`, assert those custom fields survive
      (regression test for the bug where every re-sync silently reset them
      back to defaults).

**Authos `/duster/pull`**
- [ ] Owner pulling their own app's config by `client_id` gets `200` with the
      plaintext client secret.
- [ ] A different tenant's service-account token pulling someone else's
      `client_id` gets `403` (regression test for the bug where any Duster
      CLI credentials could dump any other tenant's client secret).

**dstr-cli**
- [ ] `dstr credentials save` against a live duster + authos-api.
- [ ] `dstr sync` end-to-end, asserting the app record in Redis
      (`duster:app:id:<clientId>`) matches what Authos returned.

## Learnings from the aborted scaffold attempt (keep in mind when building this)

An initial Playwright scaffold was created and rolled back after being told to
just write this plan up instead. Worth preserving what it surfaced:

- This dev machine runs IntelliJ + several gradle/kotlin daemons + a browser
  already close to its 14GB RAM / 8GB swap ceiling. Launching authos-api via
  `./mvnw spring-boot:run` or duster via `./gradlew run` keeps a heavy
  build-tool JVM alive for the service's whole lifetime and caused real OOM
  kills (exit 137) for both during this session. Any fixture that needs to
  (re)start a service should build once and run the plain jar instead:
  `./gradlew :duster:buildFatJar` → `java -Xmx512m -jar duster/build/libs/fat.jar`,
  `./mvnw -q -DskipTests package` → `java -Xmx768m -jar authos-api/target/Authos-*.jar`.
- This machine also runs authos-api/duster directly from IntelliJ run
  configs on the same ports (8080/8785) an automated suite would target. A
  process supervisor fighting the IDE for those ports just produces
  `BindException`/`Address already in use` failures. Default to attaching to
  whatever's already listening; fail fast with a clear message instead of
  force-restarting.
- `DUSTER_ADMIN_TOKEN` must be set identically everywhere (docker-compose
  default is `dev-admin-token`) or internal-API-dependent fixtures 401. The
  IntelliJ run config for duster does not set it by default.
- Playwright + Chromium installs cleanly here via `npx playwright install
  chromium` (no sudo). The `--with-deps` variant needs sudo and fails in this
  environment — don't use it.

## Open questions before implementation starts

- CI target: does this need to run in a pipeline (needs the disposable-jar
  fixture), or is local-only (attach-to-running-services) sufficient for now?
- Should HTTP-level tests live in `automation-tests/` or closer to each
  service (e.g. Kotlin tests inside `duster/src/test`)? Proposal above keeps
  them together so one suite proves the whole stack, but this trades off
  against each repo owning its own contract tests.
