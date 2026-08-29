# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Authos serves developers who need real authentication in their apps and would rather
self-host it than adopt a hosted IdP or hand-roll OAuth. The same person usually shows
up in more than one role:

- **Integrators** — wiring an app to Authos through Duster (the BFF proxy) or the
  `@authoss/duster-*` SDK. They optimise for the fewest lines of app code and the
  fewest new services to run.
- **Operators** — running the Authos IdP + Duster: registering client apps, managing
  app groups / SSO / MFA policy, inspecting sessions and tokens, via the
  `authos-frontend` dashboard and the `dstr` CLI.
- **Evaluators** — developers deciding whether Authos is worth adopting, via the guided
  demo, the docs site, and the source. This audience is first-class: Authos is also a
  flagship portfolio piece meant to be seen and judged as engineering work.

End users of an integrator's app (the people who actually log in) meet the Authos login,
consent, and MFA screens but are not the customer.

## Product Purpose

Authos is a self-hosted OpenID Connect identity provider paired with a zero-code
integration layer (Duster). It exists so a developer can add genuine OIDC auth —
authorization code + PKCE, refresh, SSO across an app group, consent, revocation,
TOTP MFA — to their apps by running one or two containers and pointing a redirect at
them, instead of accepting a hosted IdP's lock-in and per-MAU pricing or writing OAuth
by hand.

Success: an integrator gets a working login → consent → session → silent-refresh →
logout flow against their own Authos in minutes with no OAuth code in their app; an
operator manages every client app and policy from one dashboard; and the whole stack
deploys from one artifact set on anything from `docker run` to Kubernetes, with data
never leaving the operator's infrastructure and tokens never reaching the browser.

## Positioning

**Duster** is the mechanism a neighbouring product could not truthfully copy: a thin BFF
proxy in front of the Authos IdP that collapses the entire OAuth dance into a redirect
and a `GET /me`. Tokens live in Redis behind an HttpOnly cookie; the browser never
handles them. An app integrates at the **lowest tier that covers it** — 0 (same-origin
reverse-proxy, zero app code), 1 (cross-origin + per-app CORS), 2 (backend BFF) — and
the `@authoss/duster-{core,react,vue,angular}` SDKs make tier 0/1 a `<DusterProvider>`
plus `useDuster()`.

Every Duster capability is a repackaging of a real Authos endpoint, so the tier ladder
rides a hard dependency spine on OIDC correctness (discovery document, true `expires_in`,
RFC 7009 revocation, RFC 6749 §5.2 error contract, PKCE S256). And the same Duster image
runs unchanged across `docker run`, three compose topologies, raw Kubernetes manifests,
a Kustomize base + overlay, and a bare JVM — "delegate auth with no code, deploy it
easily" is the entire claim, now demonstrated by Duster's first real cluster deployment.

## Operating Context

- **Monorepo:** `authos-api` (Spring Boot OIDC IdP), `authos-frontend` (React admin /
  login / consent UI), `duster` (Ktor BFF), `dstr-cli` (Kotlin CLI), `e2e-tests`
  (Kotlin/JUnit full-stack suite), `packages/` (the `@authoss/duster-*` npm SDK
  workspace).
- **Runtime:** PostgreSQL (Flyway) + Redis behind the API; Redis alone behind Duster;
  RS256 JWT via a PKCS12 keystore.
- **Live:** app at `https://authos.tosak.net`, API at `https://authos-api.tosak.net`
  (note: **not** `api.authos.tosak.net`), Duster in-cluster in the `authos` namespace on
  a shared cluster Redis.
- **Deploy model:** Docker Hub images → a private infra repo (`hetzner-cloud-infra`)
  Kustomize overlay → ArgoCD GitOps on a Hetzner Kubernetes cluster. Every deploy is
  gated on the e2e suite; nothing ships from `master` unless the stack suite is green.
- **Planned surfaces:** a guided walkthrough demo at `authos-demo.tosak.net`; a
  reference documentation site at `docs.tosak.net/authos` (lowercase path).
- Registering a client app is a dashboard-session action — there is no headless
  registration API. `dstr` covers Duster-side app config, credential storage, and sync.
- Documentation is **dual-audience by design decision**: human developers and AI agents
  both read it, so it commits to stable structure, canonical terminology, and a
  machine-readable form (raw Markdown / `llms.txt`) alongside the rendered site.

## Capabilities and Constraints

- **OIDC endpoints:** `/oauth/authorize` (PKCE S256), `/oauth/token` (`authorization_code`
  + `refresh_token`; `client_credentials` is Duster-service-account only),
  `/oauth/userinfo`, `/oauth/revoke` (RFC 7009), `/.well-known/openid-configuration`,
  `/.well-known/jwks.json`. `prompt=none` mints a code straight from the SSO session.
  Advertised scopes: `openid profile email offline_access`.
- **Core concepts:** **AppGroup** (groups client apps; owns SSO + MFA policy), **PPID**
  (pairwise pseudonymous `sub` per group — `SHA-256(groupId + userId + salt)`),
  **ShortSession** (Redis bridge across authorize → login → consent), **SSOSession**
  (Redis auth-state tracking).
- **MFA:** TOTP.
- **Duster tiers:** 0–2 shipped. Tier 3 (RFC 7662 introspection, token-forwarding BFF,
  resource-server `aud`) and tier 4 (RFC 8628 device flow, public PKCE clients) are
  planned, not built.
- **Rate limiting:** Redis fixed-window on `/register`, `/oauth-login`, `/native-login`
  (by client IP) and `/oauth/token` (by `client_id`).
- **SDK:** framework-agnostic `@authoss/duster-core` (owns the whole wire contract) +
  thin React / Vue / Angular adapters, published to npm via CI trusted publishing (OIDC,
  SLSA provenance).
- **Stable terminology** (must not drift): Authos, Duster, `dstr`, AppGroup, PPID, tier
  0–4, `duster:*` Redis keys, `@authoss` npm org.
- **Licensing:** GPL-3.0 at the repo root; `packages/` (the SDK) is MIT. Confirmed.
- **Explicitly undecided / not built:** structured audit logging (currently `println`),
  JWKS rotation strategy, custom / authorization-claim mapping into the ID token and
  userinfo, Phase 2+ browser and CLI test coverage. The 3-app SSO showcase and the
  Vue/Angular demo variants are deferred until the React demo ships.

## Brand Commitments

- **Names (fixed):** **Authos** (the IdP and the ecosystem), **Duster** (the BFF proxy),
  **dstr** (the CLI), **`@authoss`** (npm org — `authos` was taken).
- **Domains:** `authos.tosak.net`, `authos-api.tosak.net`, and the planned
  `authos-demo.tosak.net` and `docs.tosak.net/authos`. Hostnames stay **one
  subdomain level deep** (`<project>-<service>.tosak.net`, hyphen-joined, never
  `x.y.tosak.net`) — Cloudflare's free-plan TLS/proxy covers `*.tosak.net` only.
  Paths are unconstrained (`docs.tosak.net/authos`).
- **Voice:** precise, technical, honest about built vs planned, cites the relevant RFCs,
  no marketing inflation. The tracking docs already hold this line.
- **No visual identity is committed.** The current `authos-frontend` look — teal→cyan
  gradient wordmark, dark `#111827`, Ubuntu, shadcn defaults, an "ENTERPRISE FEATURES"
  badge, "Secure, scalable authentication" boilerplate, still on the Vite default
  favicon — is an **anti-reference**: evidence of what exists, free to be replaced.

## Evidence on Hand

- A working, deployed stack: `https://authos.tosak.net` + `https://authos-api.tosak.net`
  with a live discovery document and JWKS.
- `e2e-tests/` (Kotlin/JUnit over docker-compose) proving authorize → PKCE token →
  userinfo, `prompt=none`, revoke, the error contract, and the full Duster tier-0 flow
  (login → silent refresh → logout → upstream revoke).
- `packages/e2e/` (Playwright) driving `packages/examples/react-vite` through the real
  flow against a compose stack.
- `@authoss/duster-{core,react,vue,angular}` published to npm (v0.1.1, with provenance).
- The `docs/` tracking set: `roadmap.md`, `duster-v1-tasks.md`, `CHANGELOG.md`,
  `duster-v1-design.md` (numbered design decisions), `README.md` (doc governance).
- Duster's first cluster deployment (2026-08-29) — the portability claim, demonstrated.
- **Absent — must not be fabricated:** external users, adoption or traffic numbers,
  testimonials, case studies, press, benchmarks, revenue, a company or team. Authos is
  solo-built; no third party has deployed it, and Duster's cluster deploy is the first
  deployment of Duster anywhere.

## Product Principles

1. **Lowest tier that covers the app.** Every integration path is defined by how little
   the adopter must do; the product's job is to make the zero-code path genuinely
   sufficient for most apps.
2. **Duster is only as strong as the Authos endpoint beneath it.** Capabilities ship on
   a dependency spine — OIDC correctness leads, BFF convenience follows.
3. **One artifact set, every target.** Deployability is a feature: the same image and
   manifests must work from `docker run` to Kubernetes or the "deploy it easily" claim
   is void.
4. **Honest surface.** Discovery documents, error responses, capability lists, and docs
   reflect exactly what the code does — no advertised capability that isn't wired, no
   claim the stack can't back.
5. **Dual-audience artifacts.** Reference material is built for human developers and AI
   agents alike — stable structure, canonical terms, machine-readable alongside rendered.

## Accessibility & Inclusion

No product-specific standard has been set. The login, consent, and MFA screens are
reached by end users of adopter apps and are security-critical, so WCAG 2.1 AA is the
sensible floor for those surfaces — treat this as an open decision to confirm, not a
recorded requirement.
