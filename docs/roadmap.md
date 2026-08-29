# Authos + Duster — Roadmap

**This file is the plan** — the multi-phase arc and the tier→capability dependencies. It changes
only when the *plan* changes, which is rare and deliberate. It carries **no live status**: what is
done vs. outstanding lives in `duster-v1-tasks.md`, the dated history in `CHANGELOG.md`, the
rationale in `duster-v1-design.md`. Feature branches must not edit this file.

---

## Premise

Duster is a thin BFF. Every capability it offers is a re-packaging of something the **Authos API**
does — `authorize`, `token`, `refresh`, `userinfo`, and (not yet) `revoke` / `introspect`. A Duster
feature can only be as robust as the Authos endpoint beneath it.

So the integration-tier ladder from `duster-v1-design.md` #21 has a hard dependency spine on the
Authos side:

| Duster tier | Authos capability it needs |
|---|---|
| **0** — zero-code frontend | auth code + refresh + `prompt=none`; real `expires_in`; PKCE |
| **1** — cross-origin frontend | same as tier 0 |
| **2** — backend BFF *(current model)* | + token revocation (RFC 7009); `end_session_endpoint` |
| **3** — token-forwarding BFF | + token introspection (RFC 7662); resource-server `aud`; generic `client_credentials` |
| **4** — native / device | + Device Authorization Flow (RFC 8628); public clients (PKCE, no secret) |
| *all tiers* | discovery doc; custom / authz claims; JWKS rotation; audit + rate limiting |

This refines the "Duster production-readiness first, then Authos" ordering: Duster-first still holds
**within** a phase, but the Authos gaps that *block* a tier (PKCE, revocation, introspection, device
flow) lead their phase.

---

## Phase 0 — Authos OIDC core hardening

*Blocks every tier. Nothing else should land on top of the current core.*

**Authos**
- **PKCE, end to end.** `code_challenge` / `code_challenge_method` at `/oauth/authorize` (S256
  only), `code_verifier` verified at `/oauth/token` — a modifier on the `authorization_code` grant.
  "Verify if present" + RFC 7636 §4.6 downgrade protection.
- **`GET /.well-known/openid-configuration`.** Issuer, endpoint URLs, `jwks_uri`, and the supported
  response / grant / scope / claim / PKCE lists — each reflecting what the code actually does. The
  `issuer` must byte-match the ID token `iss`. Stops Duster and every SDK from hardcoding paths.
- **Real `expires_in`.** `/oauth/token` must report the access token's true lifetime (it was a
  hardcoded `3600`). Duster stores the token in Redis with exactly this value, so a wrong number
  means premature or stale silent refresh.
- **Consistent OAuth errors.** RFC 6749 §5.2 JSON on `/token`, redirect-with-`error` on
  `/authorize`; no path falls through to a raw 500 / whitelabel page (Duster #28 needs a clean
  user-facing error).
- **Re-enable the `/approve` request-integrity check.** The code must be minted from the server-side
  `ShortSession`, not from the query params the browser carried through login/consent — otherwise a
  swapped `redirect_uri` or escalated `scope` between `/authorize` and `/approve` goes unchecked.

**Exit criteria:** OIDC smoke suite passes (authorize → PKCE token → userinfo, `prompt=none`, error
cases); discovery doc validates; the existing Duster flow works unchanged against the hardened core.

---

## Phase 1 — Duster tiers 0 & 1 (zero-code) + revocation

**Authos**
- **`POST /oauth/revoke`** (RFC 7009) — revoke a refresh or access token; a refresh-token revoke
  cascades to the access tokens for that grant. Unblocks Duster `/logout` revocation.
- Verify `prompt=none` silent re-auth when the Authos SSO session outlives the Duster session —
  Duster's `/session` silent refresh (#16) depends on it.

**Duster** (design decisions #22–28)
- `GET /duster/api/v1/me` — browser-facing session read (#22)
- `success_url` may be a plain SPA route at tier 0 (#22)
- `SameSite=Strict` → `Lax` (#23)
- Client-scoped cookie name (#24)
- Client-scoped token Redis keys (#25) — fixes the same-AppGroup refresh-token clobber
- `/logout` calls `/oauth/revoke` + purges token keys (#26)
- Per-app `allowed_origins` → CORS + `SameSite=None` for tier 1 (#27)
- `/callback` failures redirect to `error_url`, not 500 (#28)

**SDKs**
- `@authoss/duster-core` (framework-agnostic; also the vanilla-JS build) + `@authoss/duster-react`,
  then `-vue`, then `-angular` — thin adapters over the core, pointed at `/me`, no backend assumed
  (`duster-v1-design.md` #11, #31)

**Exit criteria:** a static-hosted SPA with only a proxy rule + the React snippet can log in, survive
silent refresh, and log out with the upstream token actually revoked.

---

## Phase 2 — Duster tier 2 hardening + headless onboarding

**Authos**
- **Machine client-registration API** — token-authenticated `POST /oauth/clients` (or an extension
  of `/duster/*`). `ApplicationController`'s `/app/*` endpoints are dashboard-session only, so
  `dstr init` can't create an Authos app headlessly.
- **`end_session_endpoint`** (OIDC RP-Initiated Logout) + SSO-group logout propagation — logging out
  of one app in an AppGroup ends the shared SSO session.
- **Refresh-token rotation + reuse detection** — rotate the value on every `refresh_token` grant,
  flag the token family on reuse of a retired value. Needed before Duster sessions (which refresh on
  every `/session` check) are safe to run for days.

**Duster**
- Webhook retry + `duster:webhook:dlq:<clientId>` + `webhook_required` gate (#29)

**dstr-cli**
- `dstr init` interactive wizard (#13) — now possible against the registration API

**Exit criteria:** `dstr init` takes a developer from zero to a working tier-2 integration in one
command; provisioning webhooks survive a flaky backend.

---

## Phase 3 — Duster tier 3 (token-forwarding)

**Authos**
- **`POST /oauth/introspect`** (RFC 7662) — replaces the bespoke `/duster/validate-token` stub.
- **Resource-server audiences** — issue access tokens with an `aud` for a named downstream API;
  `/oauth/userinfo` and introspection validate it.
- Generalize `client_credentials` beyond `dusterAppService.validateAppCredentials` so non-Duster
  M2M clients work.

**Duster**
- Forwarding proxy endpoint — Duster attaches `duster:token:<clientId>:<sub>:access` to an outbound
  call to a declared downstream API (`duster-v1-design.md` #30).

**Exit criteria:** a tier-2 app calls a separate resource API through Duster without ever handling a
token.

---

## Phase 4 — Duster tier 4 (native / device)

**Authos**
- **Device Authorization Flow** (RFC 8628) — `/oauth/device_authorization` + `device_code` grant.
  Also unblocks `dstr auth login` (#14), currently on a PAT fallback.
- **Public clients** — PKCE-only token exchange with no `client_secret`.

**Duster**
- Opaque session-token endpoint + bearer acceptance on `/me` (no cookie) — the transport
  independence #30 asks the session layer to keep.

**Exit criteria:** a CLI or mobile client authenticates via device flow and holds a Duster session
with no browser cookie.

---

## Cross-cutting (every phase)

- **Audit logging** — replace `println` on the Authos auth paths with structured, queryable events
  (login, consent, token issue, revoke).
- **Rate limiting / brute-force protection** on `/oauth-login`, `/oauth/token`, `/register`.
- **JWKS rotation strategy** — publish overlapping `kid`s before rotating the signing key; Duster
  and every SDK cache JWKS and break on a hard swap.
- **Custom / authorization claims** — map roles / groups / entitlements into the ID token + userinfo
  so real apps at any tier get authz data, not just profile.
- **Test coverage** — the `e2e-tests/` module stands up a real Postgres + Redis + authos-api +
  duster stack. Each phase extends it before it closes; browser specs + `dstr-cli`-binary coverage
  are Phase 2 (`automation-tests-plan.md`).

---

## The doc set

`docs/README.md` has the full table (what each file is, who edits it and how) plus
the size guardrail and the archiving procedure. In short:

| file | what | who edits it |
|---|---|---|
| `roadmap.md` (this) | the plan: phases, tier→capability spine | only when re-planning |
| `duster-v1-tasks.md` | live checklist, one box per item | feature branches — flip `[ ]`→`[x]` in place |
| `CHANGELOG.md` | dated one-line history of what landed | feature branches — append at the end |
| `duster-v1-design.md` | numbered decision log (the *why*) | when a decision is made |
| `automation-tests-plan.md` | test strategy | when the test plan changes |
