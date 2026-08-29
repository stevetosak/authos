# Changelog

Dated, one-line-per-change history of what has landed on `master`. Newest entries at the **end**.

**Editing rule:** only ever *append* a new line (or a small block) at the end. Never reword,
reorder, or delete an existing entry. `.gitattributes` marks this file `merge=union`, so parallel
branches that each append their own line merge cleanly with no conflict — but that guarantee holds
only as long as nobody edits existing lines.

Format: `- YYYY-MM-DD — <area>: <what changed> (PR #N)`

When this file passes ~500 lines, `.github/scripts/archive-changelog.sh` rolls the oldest entries
into `archive/CHANGELOG-archive.md`. See `README.md` § Archiving.

---

- 2026-06-14 — docs: Duster v1 design decision log finalized (#1–20)
- 2026-06-14 — duster: env-based config, deployment topology, monorepo Gradle setup (PR #20, #21, #22)
- 2026-08-28 — duster: auth-flow refactor — client-scoped sessions, Duster-side PKCE, HMAC-SHA256 callback signing, JSON callback contract, `DUSTER_ADMIN_TOKEN` gate on `/internal/*`, `PATCH /internal/apps/config`, upsert-safe `/internal/apps/create`, silent session refresh, `SameSite=Strict` cookie, opt-in k8s ingress (PR #23)
- 2026-08-28 — authos+frontend: consent screen renders real client name / scopes / user email via `GET /oauth/client-info`; `/duster/pull` rejects cross-tenant reads (403); `GET`/`POST /test/callback` reference landing route + webhook contract fix (PR #23)
- 2026-08-28 — docs: adaptability review — design decisions #21–30 (integration tier ladder 0–4; v1 ships tiers 0–2)
- 2026-08-28 — ci: commit the root `gradle-wrapper.jar` + add `wrapper-validation` so the e2e workflow can bootstrap `gradlew`
- 2026-08-28 — authos: PKCE end to end at `/oauth/authorize` + `/oauth/token` (S256 only, verify-if-present, RFC 7636 §4.6 downgrade protection) (PR #24)
- 2026-08-28 — e2e-tests: new Gradle module — docker-compose stack (Postgres + Redis + authos-api + duster) + HTTP-level suite + `e2e.yaml` workflow (PR #25)
- 2026-08-29 — authos: `/oauth/token` reports the access token's real `expires_in`; new `authos.oidc.access-token-ttl-seconds` (default 3600); access-token lifetime 24h → 1h (PR #26)
- 2026-08-29 — authos: OIDC discovery doc at `GET /.well-known/openid-configuration` — `issuer` byte-matches the ID token `iss`, capability lists match the implementation (PR #27)
- 2026-08-29 — docs: split tracking into plan (`roadmap.md`) / checklist (`duster-v1-tasks.md`) / append-only `CHANGELOG.md`; added `docs/README.md`, a `.github/scripts/doc-size.sh` line-limit gate (CI: `docs.yaml`), and `.github/scripts/archive-changelog.sh`
- 2026-08-29 — authos: consistent OAuth errors — RFC 6749 §5.2 JSON on `/token`, redirect-with-`error` on `/authorize`, path-aware catch-all, `invalid_client` → 401; `/approve` mints the code from the server-side `ShortSession` (PR #28)
- 2026-08-29 — authos: `POST /oauth/revoke` (RFC 7009) — client-authenticated, always 200, refresh-token revoke cascades to that grant's access tokens, other-client/unknown tokens are a no-op; discovery doc now advertises `revocation_endpoint`
- 2026-08-29 — authos: `prompt=none` silent re-auth now mints the code straight from the SSO session instead of bouncing through `/oauth/approve`, so it no longer 401s once the short-lived `AUTH_TOKEN` login cookie lapses while the SSO session is still valid; `e2e-tests/PromptNoneTest` (PR #33)
- 2026-08-29 — duster: `GET /duster/api/v1/me` — browser-facing alias of `/session` for the tier-0 SPA / frontend SDK (design #22); one shared handler, `401` when the `duster_session` cookie is absent or dead; `e2e-tests/DusterMeEndpointTest` (PR #34)
- 2026-08-29 — duster: `success_url` / `logout_redirect_url` may be a root-relative SPA route (tier 0, design #22) or an absolute http(s) URL — `PATCH /internal/apps/config` validates and rejects anything else (`//host`, `javascript:`, bare words); `/start` and `/callback` fall back to `/` for a stored invalid value; `e2e-tests/DusterSuccessUrlTest` (PR #35)
- 2026-08-29 — duster: `duster_session` cookie is now `SameSite=Lax` (was `Strict`), so a cross-site top-level navigation into the app keeps the session; the `/logout` clear mirrors it (design #23); `e2e-tests/DusterCookieAttributesTest` (PR #36)
- 2026-08-29 — duster: the session cookie name is now `duster_session_<clientId>` (design #24) — two Duster-backed apps proxied under one domain no longer collide on a bare `duster_session` and overwrite each other's session; `/start` `/callback` `/session` `/me` `/logout` all use the scoped name (PR #37)
- 2026-08-29 — duster: token Redis keys are now `duster:token:<clientId>:<sub>:<type>` (was `<type>_token:sub:<sub>`) — two apps in one AppGroup share a pairwise `sub`, so the old keys let the second login's `saveAll` clobber the first's refresh token (design #25); `e2e-tests/DusterClientScopedTokenTest` (PR #38)
- 2026-08-29 — duster: `GET /duster/api/v1/logout` now revokes the grant at Authos (`POST /oauth/revoke`, best-effort) and purges the `duster:token:<clientId>:<sub>:*` keys, not just the local session (design #26); `e2e-tests/DusterLogoutRevokesTest` (PR #39)
- 2026-08-29 — duster: tier-1 cross-origin support — `DusterApp.allowed_origins` (set via `PATCH /internal/apps/config` / `dstr apps configure --allowed-origins`); when non-empty Duster answers credentialed CORS for those exact origins on the browser-facing endpoints (preflight + `/me`) and issues the `duster_session` cookie `SameSite=None; Secure`; empty (default) keeps the tight no-CORS / `SameSite=Lax` posture (design #27, part 1 of 2); `e2e-tests/DusterCorsTest` (PR #40)
- 2026-08-29 — duster: tier-1 logout CSRF — `POST /duster/api/v1/logout` + `X-Duster-Csrf` synchronizer token (returned by `/me`, stored on the `DusterSession`) required for apps with `allowed_origins` set; `GET /logout` still serves tier-0/2. Closes the CSRF hole `SameSite=None` opens (design #27, part 2 of 2); `e2e-tests/DusterLogoutCsrfTest` (PR #41)
- 2026-08-29 — duster: `/callback` now redirects a failed OAuth exchange to the app's `error_url` (new `DusterApp.error_url`, config API + `dstr apps configure --error-url`; defaults to the `success_url` origin + `/error`) instead of a raw 500; a pre-app failure (bad `state`) falls back to `/error` (design #28); `e2e-tests/DusterCallbackErrorTest` (PR #42)
- 2026-08-29 — docs: design decision #31 — frontend SDK is a framework-agnostic `@authoss/duster-core` + thin React/Vue/Angular adapters (core also = the vanilla-JS build); in-monorepo `packages/` npm workspace, public npm via trusted publishers, no polling; split license (GPL-3.0 root, MIT packages); archived shipped decisions #4 + #8 + #15 to `archive/duster-v1-design-archive.md` to stay under the size cap
- 2026-08-29 — sdk: `@authoss/duster-core` — framework-agnostic Duster browser client (observable store + `/me` normalization with a `raw` passthrough, `X-Duster-Csrf` capture/replay, always-POST logout, `onUnauthenticated` dispatch, SSR guards, opt-in focus/reconnect revalidation, IIFE `window.Duster` build); new `packages/` npm workspace + `sdk.yaml` unit CI; 53 Vitest specs against a wire-contract mock (PR #44)
- 2026-08-29 — sdk: `@authoss/duster-react` — `<DusterProvider>` (flat config props or `config={}`) + `useDuster() → {user, status, login, logout, refresh, error}` + `<ProtectedRoute>`/`<Protected>`; `useSyncExternalStore` bridge over the core store, one client per `clientId` (StrictMode / multi-provider safe), no router dep; 25 RTL/jsdom specs; `sdk.yaml` gains a `react` vitest project (PR #45)
