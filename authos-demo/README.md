# authos-demo — The Handshake Trace

A live, guided walkthrough of a real OpenID Connect login, built on the published
[`@authoss/duster-react`](https://www.npmjs.com/package/@authoss/duster-react). A first-time
visitor presses **Run**, is redirected through a real [Authos](https://authos-api.tosak.net)
login, returns authenticated, watches the captured exchange play back as a sequence diagram on a
time base, forces a silent refresh, and logs out — leaving able to say *"one redirect and a
`GET /me`"*, trusting the browser never held a token.

Deployed at **https://authos-demo.tosak.net**.

## What it demonstrates (tier 0)

- `login()` → one top-level redirect to `/duster/api/v1/oauth/start`
- Consent on Authos — never on this page
- Duster exchanges the code, verifies the ID token, sets a `HttpOnly` cookie, 302s home
- `GET /me` — the only call the SPA makes; flat JSON + `X-Duster-Csrf`
- A forced `/me` (silent refresh) — still `200`, no browser timer
- `POST /logout` → Duster revokes the grant at Authos and purges `duster:token:*`

The messages the browser genuinely makes are drawn solid with their real status/latency; the
server-to-server and top-level-redirect hops are drawn as the protocol and labelled *narrated*,
with a link to the `packages/e2e` spec and CI run that prove them on every push.

## Local development

```bash
npm install
npm run dev        # http://localhost:5175
```

`npm run dev` renders the UI and the unauthenticated trace. It does **not** run a live OAuth
round-trip (that needs the full stack and HTTPS cookies). Two ways to see more:

| URL | What you get |
|-----|--------------|
| `?replay=ready` | canned "nothing run yet" capture — the dim protocol drawing |
| `?replay=1` | canned authenticated capture — `/me` body, token trace HIGH, wire log |
| `?client_id=<id>` against a seeded local stack | the real flow (see below) |

For the real flow locally, bring up `e2e-tests/docker-compose.e2e.yml` (Duster on host
`:18785`, which `vite.config.ts` proxies `/duster` to), seed an app, and open
`http://localhost:5175/?client_id=<the app's client_id>`.

## Scripts

| Script | Purpose |
|--------|---------|
| `npm run dev` | Vite dev server, port 5175 |
| `npm run build` | `tsc --noEmit` + `vite build` → `dist/` |
| `npm run preview` | serve the production build |
| `npm run typecheck` | `tsc --noEmit` |
| `npm run lint` | ESLint |
| `npm run bootstrap` | one-time provisioning against the live stack (see below) |

## Provisioning (`npm run bootstrap`)

Run once against the live stack to create the Duster-wired OAuth app and print its `client_id`:

```bash
DEMO_OWNER_EMAIL=demo-owner@… \
DEMO_OWNER_PASSWORD=… \
DEMO_DUSTER_ADMIN_TOKEN=… \
DEMO_DUSTER=http://localhost:8785 \   # a `kubectl port-forward svc/duster 8785:8785`
npm run bootstrap
```

It is re-entrant — a second run reuses the existing app. Registering a fresh Authos app mints a
**new** `client_id`, so run it once and set the printed value as the repo variable
`DEMO_DUSTER_CLIENT_ID` (it flows into the deploy overlay; `entrypoint.sh` renders it into
`config/config.js` at container start — never baked into the image).

## Deployment

`Dockerfile` serves the CI-built `dist/` from nginx; `.github/workflows/demo.yaml` gates on the
e2e stack suite, builds + pushes `stevetosak/authos-demo`, and bumps the infra-repo overlay
(`INFRA_REPO_DEMO_OVERLAY_DIR`). `k8s/` is the reference Kustomize base + example overlay: a
Deployment, a Service, and the **Ingress** for `authos-demo.tosak.net` whose `/duster` path
forwards verbatim (no rewrite) to the in-cluster `duster` Service — the tier-0 same-origin
arrangement. The demo runs in the `authos` namespace alongside Duster.
