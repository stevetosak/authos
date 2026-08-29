# Duster v1 — Task Checklist

The plan (phases, tier→capability spine) is `roadmap.md`. The rationale is `duster-v1-design.md`
(bracketed `#N` below point at its numbered decisions). The dated history of what landed is
`CHANGELOG.md`. How the doc set fits together, plus the size guardrail, is `README.md`.

**Editing rule for feature branches:** flip `[ ]` → `[x]` in place. Do not move, reword, reorder,
or regroup lines. An optional ` — PR #N` suffix is fine. Anything else — new items, restructuring,
re-scoping — is a plan change: make it in a dedicated docs branch, never in a feature branch. This
is what keeps the file merge-conflict-free across parallel branches.

---

## Phase 0 — Authos OIDC core hardening

- [x] PKCE end to end — S256 only, verify-if-present, RFC 7636 §4.6 downgrade protection — PR #24
- [x] `GET /.well-known/openid-configuration` discovery doc — PR #27
- [x] Real `expires_in` on `/oauth/token` — PR #26
- [x] Consistent OAuth errors — RFC 6749 §5.2 JSON on `/token`, redirect-with-`error` on `/authorize`, no raw 500 / whitelabel page — PR #28
- [x] Re-enable the `/approve` request-integrity check — mint the code from the server-side `ShortSession`, not the carried-through query params — PR #28

## Phase 1 — Duster tiers 0 & 1 (zero-code) + revocation

Authos:
- [x] `POST /oauth/revoke` (RFC 7009) — refresh-token revoke cascades to that grant's access tokens
- [x] Verify `prompt=none` silent re-auth when the Authos SSO session outlives the Duster session (#16) — PR #33

Duster:
- [x] `GET /duster/api/v1/me` — browser-facing session read (#22) — PR #34
- [x] `success_url` may be a plain SPA route at tier 0 (#22) — PR #35
- [x] `SameSite=Strict` → `Lax` on `duster_session` (#23) — PR #36
- [x] Client-scoped cookie name (#24) — PR #37
- [x] Client-scoped token Redis keys (#25) — fixes the same-AppGroup refresh-token clobber — PR #38
- [x] `/logout` calls `/oauth/revoke` + purges token keys (#26) — PR #39
- [x] Per-app `allowed_origins` → CORS + `SameSite=None` for tier 1 (#27) — PR #40, #41
- [x] `/callback` failures redirect to `error_url`, not 500 (#28) — PR #42

SDKs (#11, #31 — framework-agnostic `@authoss/duster-core` + thin adapters, pointed at `/me`, no backend assumed):
- [x] `@authoss/duster-core` — store/seam, `/me` normalization, CSRF, logout, `onUnauthenticated`; also the vanilla-JS build — PR #44
- [x] `@authoss/duster-react` — `<DusterProvider>` + `useDuster()` + `<ProtectedRoute>`; browser-e2e vs the compose stack (closes the Phase 1 exit criterion) — PR #45, #46
- [x] `@authoss/duster-vue` — `createDuster()` plugin + `useDuster()` + `<ProtectedRoute>` — PR #47
- [x] `@authoss/duster-angular` — `provideDuster()` + `DusterService` + `dusterAuthGuard` — PR #48
- [x] `sdk-release.yaml` — tag-gated lockstep `npm publish` via trusted publishers (first publish needs the one-time per-package config on npmjs.com) — PR #49

## Phase 2 — Duster tier 2 hardening + headless onboarding

Authos:
- [ ] Machine client-registration API — token-authenticated `POST /oauth/clients` (or a `/duster/*` extension)
- [ ] `end_session_endpoint` (OIDC RP-Initiated Logout) + SSO-group logout propagation
- [ ] Refresh-token rotation + reuse detection

Duster:
- [ ] Webhook retry + `duster:webhook:dlq:<clientId>` + `webhook_required` gate (#29)

dstr-cli:
- [ ] `dstr init` interactive wizard (#13)

## Phase 3 — Duster tier 3 (token-forwarding)

Authos:
- [ ] `POST /oauth/introspect` (RFC 7662) — replaces the `/duster/validate-token` stub
- [ ] Resource-server audiences — access tokens with an `aud` for a named downstream API
- [ ] Generalize `client_credentials` beyond `dusterAppService.validateAppCredentials`

Duster:
- [ ] Forwarding proxy endpoint — attach the stored access token to an outbound call to a declared downstream API (#30)

## Phase 4 — Duster tier 4 (native / device)

Authos:
- [ ] Device Authorization Flow (RFC 8628) — `/oauth/device_authorization` + `device_code` grant; also unblocks `dstr auth login` (#14)
- [ ] Public clients — PKCE-only token exchange, no `client_secret`

Duster:
- [ ] Opaque session-token endpoint + bearer acceptance on `/me` (no cookie) (#30)

## Phase 5 — Documentation site (`docs.tosak.net/authos`)

- [ ] Site scaffold + `docs.tosak.net/authos` hosting + CI deploy on docs change
- [ ] Concepts + per-tier integration guides + `@authoss/duster-*` SDK and `dstr` reference
- [ ] Full HTTP + config + error-contract reference for Authos and Duster
- [ ] AI-facing source (`llms.txt` / raw Markdown) served alongside the rendered site
- [ ] Reconcile the in-repo `docs/` tracking set with the public site (non-duplicative split)

## Phase 6 — Frontend redesign & realignment

- [ ] Audit every screen against current API contracts (consent data, error shape, discovery, revoke, Duster app config)
- [ ] Redesign — design system across login / consent / dashboard
- [ ] First-class Duster app management UI (configure / sync / credentials / webhook / `allowed_origins` / `error_url` / CSRF / TTL)
- [ ] Wire endpoints added since the last frontend pass + fix the `AuthProvider.tsx` stale-closure `setInterval` bug
- [ ] Frontend component test coverage

## Phase 7 — `dstr-cli` realignment & portability

- [ ] State audit — every command vs the current Duster + Authos surface; list the gaps
- [ ] `debug` / `inspect` group — live session (`/me`), token keys, `duster:state:*`, app config, `/health`, tier
- [ ] Feature parity — revocation, RFC 6749 §5.2 errors, discovery-doc validation, `sync` change reporting; fold in `dstr init` (#13) / `dstr auth login` (#14) as they land
- [ ] Agent-grade I/O — `--json` on every command (stable schema), documented exit codes, no non-flag prompts, a machine-readable usage manifest
- [ ] Portability — portable distribution (self-contained launcher / native image / package manager), cross-OS one-step install, CI/container-friendly config discovery

## Cross-cutting (every phase)

- [ ] Audit logging on the Authos auth paths — replace `println` with structured events
- [x] Rate limiting / brute-force protection on `/oauth-login`, `/oauth/token`, `/register` — PR #53
- [ ] JWKS rotation strategy — publish overlapping `kid`s before rotating the signing key
- [ ] Custom / authorization claims mapped into the ID token + userinfo
- [ ] Duster deployability — portable artifacts (`PORT` / `REDIS_URL`, hardened image, Kustomize base+overlay, `DEPLOY.md`)
- [ ] Deploy Duster to the cluster — first-class service + `duster.yaml` CI + GitOps overlay
- [x] `e2e-tests/` stack suite — Postgres + Redis + authos-api + duster, HTTP-level — PR #25
- [ ] Phase 2 test coverage — Playwright browser specs, `dstr-cli` binary, `authos-frontend` components

---

## Already shipped (pre–Phase 0)

The Duster BFF core landed before the phased roadmap. Frozen record; dated detail is in `CHANGELOG.md`.

Duster:
- [x] `duster_session` cookie + Redis-backed sessions isolated by `clientId`
- [x] `GET /session`, `GET /logout`
- [x] PKCE on the Duster side — `StateStore` verifier/challenge, authorize URL, token exchange
- [x] HMAC-SHA256 callback webhook signing (`X-Duster-Signature`)
- [x] JSON body callback contract, deserialized into `CallbackResponse`
- [x] `success_url` / `logout_redirect_url` / `webhook_secret` / `session_ttl` on `DusterApp`
- [x] `DUSTER_ADMIN_TOKEN` gate on `/internal/*` (was unauthenticated)
- [x] `PATCH /internal/apps/config`
- [x] Upsert-safe `POST /internal/apps/create` — re-sync no longer resets configured fields to defaults
- [x] Silent session refresh (`tryAccessTokenExchange`) + sliding Redis TTL
- [x] `SameSite=Strict` on `duster_session`
- [x] `k8s/ingress.yml` moved to `k8s/optional/` (opt-in)
- [x] `/authorize` sends `prompt=consent` when scope requires it
- [x] `success_url` default (`"/"`) 404 documented as decision #20

dstr-cli:
- [x] `dstr apps configure` (success/logout url, webhook secret, session ttl)
- [x] Admin-token plumbing — `--admin-token` / `DUSTER_ADMIN_TOKEN` / `~/.dstr/dstr.config`
- [x] `dstr credentials save` URL fix (swapped path segments)
- [x] `dstr apps` shows `successUrl` / `logoutRedirectUrl` / `sessionTtl` / `webhookSecret`

Authos:
- [x] Consent screen renders real client name, real scopes, real user email (`GET /oauth/client-info` + `GET /verify`)
- [x] `/duster/pull` rejects cross-tenant pulls (403)
- [x] `GET /test/callback` reference `success_url` landing route (#20)
- [x] `POST /test/callback` webhook returns the `200 {}` contract (was a stale `302`)
