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

## Phase 5 — Documentation site (`docs.tosak.net/authos`)

*Independent of the tier ladder — it can proceed once the surface it documents is stable, and sits
after Phase 4 only because the whole ladder should exist before it is documented as one system.*

A thorough, reference-grade explanation of the Authos stack, written to serve **both human
developers and AI agents**: stable page structure, canonical terminology, self-contained pages, and
a machine-readable source (`llms.txt` / raw Markdown) served alongside the rendered site.

- **Concepts** — the OIDC flow end to end; `AppGroup`, PPID, `ShortSession`, `SSOSession`, the
  per-token-type JWT strategy; the Duster BFF model and the tier ladder (0–4).
- **Integration guides** — one per tier, each a copy-paste path from zero to working; the
  `@authoss/duster-*` SDK surface and per-framework recipes; the `dstr` CLI.
- **Reference** — every HTTP endpoint (Authos + Duster), the discovery document, config keys, env
  vars, Redis key shapes, the RFC 6749 §5.2 error contract.
- **Operations** — deployment topologies, JWKS rotation, the compose / k8s manifests.
- **Guided demo** — an interactive walkthrough at `demo.authos.tosak.net`, the click-through
  companion to the reference: a real SPA on `@authoss/duster-react` that narrates each step of a
  live login → silent refresh → logout → revoke against the deployed stack.

Hosted on a separate domain, `docs.tosak.net`, with Authos under `/authos` (leaving sibling paths
free for other projects). Static build, deployed from CI on a docs change.

**Exit criteria:** a developer or an agent can integrate at any tier using only the site; every
concept and endpoint in the codebase has a documented counterpart; the site builds and deploys in
CI; the in-repo `docs/` tracking set and the public site have a clear, non-duplicative split.

---

## Phase 6 — Frontend redesign & realignment

*The `authos-frontend` SPA — login, consent, dashboard / admin UI — has drifted from the backend
changes made across Phases 0–1 and will drift further through 2–4. This phase re-aligns it with the
current contracts and redesigns the UI on top of the existing React/Vite build (not a rewrite).*

- **Contract audit** — every screen against the current API: real consent data, the RFC 6749 §5.2
  error shape, the discovery document, revocation, Duster app config (`allowed_origins`, `error_url`,
  logout CSRF, session TTL).
- **Redesign** — a coherent design system across the login, consent, and dashboard surfaces;
  first-class Duster app management (create / configure / sync, credential display, webhook secret).
- **Wire-up** — connect the endpoints added since the last frontend pass; surface token / session
  state honestly.
- **Fix known drift** — the dead `setInterval` in `AuthProvider.tsx` (the `useEffect([])` closure
  captures a stale `isAuthenticated`), plus whatever the audit turns up.

**Exit criteria:** every current API path has a frontend counterpart; the consent and admin flows
match backend behaviour exactly; the redesign is shipped; the auth-context refresh bug is fixed;
frontend component coverage lands with it (feeds the Cross-cutting test-coverage item).

---

## Phase 7 — `dstr-cli` realignment & portability

*`dstr-cli` has drifted the same way the frontend has: it was built against an earlier Duster and
knows nothing about the Phase 0–4 additions. This phase audits it, closes the feature gap, and makes
it the primary way to operate and debug Duster — for a human at a terminal and for an AI agent
driving it non-interactively.*

**North star:** `dstr` is the best tool to both *use* and *debug* Duster. A human should reach for it
before `curl` or `redis-cli`; an agent should be able to script the whole provisioning + inspection
surface from it with stable output and exit codes.

- **State audit** — inventory every command against the current Duster + Authos surface. Known gaps:
  no way to inspect a live session (`/me` / `/session`), the stored token keys
  (`duster:token:<clientId>:<sub>:*`), the pending `duster:state:*` entries, or `/health`; no
  awareness of revocation, the discovery document, `allowed_origins` / `error_url` / logout-CSRF /
  session-TTL semantics, or the tier a given app is running at.
- **Feature parity** — bring the command tree up to the Phase 0–2 reality: a `debug` / `inspect`
  group (session, tokens, state, app config, health, tier); surface revocation and the RFC 6749 §5.2
  error contract in output; validate config against the live discovery doc; make `sync` report what
  changed. Fold in `dstr init` (#13) and `dstr auth login` (#14) as they land in Phases 2 and 4.
- **Agent-grade I/O** — a `--json` mode on every command (stable schema), documented exit codes,
  fully non-interactive operation (no prompt that can't be a flag), and a short machine-readable
  capability/usage manifest an agent can load. The human path keeps the Mordant tables.
- **Portability** — today `dstr` needs a JVM and a Unix `install.sh` → `~/.local/bin/dstr`. Decide
  and ship a portable distribution: a self-contained launcher, a native image, and/or a package
  manager path; cross-OS install; config discovery that works from CI and containers. This mirrors
  the Duster deployability work — the CLI must travel as easily as the service it manages.

**Exit criteria:** every Duster/Authos capability through Phase 2 is reachable from `dstr`; `dstr`
can provision, inspect, and tear down a Duster app end to end without the dashboard or raw HTTP;
`--json` output is stable enough to script against; the CLI installs in one step on Linux, macOS,
and CI without a manual JDK setup.

---

## Cross-cutting (every phase)

- **Audit logging** — replace `println` on the Authos auth paths with structured, queryable events
  (login, consent, token issue, revoke).
- **Rate limiting / brute-force protection** on `/oauth-login`, `/oauth/token`, `/register`.
- **JWKS rotation strategy** — publish overlapping `kid`s before rotating the signing key; Duster
  and every SDK cache JWKS and break on a hard swap.
- **Custom / authorization claims** — map roles / groups / entitlements into the ID token + userinfo
  so real apps at any tier get authz data, not just profile.
- **Deployment** — Duster runs in the cluster as a first-class service (image + CI + GitOps
  overlay), like `authos-api` / `authos-frontend`. This is Duster's first deployment anywhere and
  the test of its "deploy easily" promise, so the work is portability-first: one artifact set that
  installs cleanly via `docker run`, docker-compose, raw k8s, Kustomize base+overlay, and bare JVM,
  with a `DEPLOY.md` a third party could follow.
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
