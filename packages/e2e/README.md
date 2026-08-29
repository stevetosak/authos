# @authoss/duster-browser-e2e

Playwright suite that drives the [`examples/react-vite`](../examples/react-vite) SPA — built against
the real `@authoss/duster-react` package — through **login → silent refresh → logout** against the
`e2e-tests/docker-compose.e2e.yml` stack, and asserts the upstream refresh token is gone from Redis
after logout. This is the Phase 1 exit criterion for the SDK.

## Run

The stack is brought up by the caller (see [`../README.md`](../README.md#browser-e2e) or the
`browser-e2e` job in `.github/workflows/sdk.yaml`). Then:

```bash
npm run seed     # register a user + a tier-0 Duster app, sync it → writes .fixture.json
npm test         # Playwright (globalSetup re-runs the seed; webServer builds + previews the SPA)
```

## How the IdP hop works

The compose stack has no `authos-frontend`, so:

- `page.route` fetches `/duster/api/v1/oauth/start` and rewrites the `Location` from the
  compose-internal `http://authos-api:8080` to the host-mapped `http://localhost:18080` (Chromium
  follows a top-level 3xx internally, so the redirected request can't be intercepted directly).
- Authos then 302s the browser to `http://localhost:5173/oauth/login?…` (`FRONTEND_HOST`), which is
  the example's own preview server — the spec reads `authz_id` / `state` off the URL.
- `oauth-flow.ts` scripts `POST /oauth-login` + `GET /oauth/approve` with an `APIRequestContext`
  (a port of `e2e-tests/.../support/OAuthFlow.kt`), then the browser is navigated onto Duster's
  `/callback` through the tier-0 proxy.

`redis-probe.ts` is a ~90-line RESP client (port of `support/RedisProbe.kt`) → compose Redis on
`localhost:16379` (override with `E2E_REDIS=host:port`).
