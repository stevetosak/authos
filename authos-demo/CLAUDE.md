# CLAUDE.md — authos-demo

Guidance for Claude Code working in this module.

## What this is

**The Handshake Trace** — the public, guided walkthrough of a real Authos + Duster tier-0 OIDC
login, at `https://authos-demo.tosak.net`. A standalone React 19 / Vite 7 SPA (not a Gradle
module, not part of the `packages/` workspace) that consumes the **published**
`@authoss/duster-react` from npm — the same package, unmodified, an integrator would `npm i`.

Visual direction: a UML/RFC sequence diagram (`BROWSER · DUSTER · AUTHOS` lifelines, message
arrows) on an oscilloscope time base, with the access token as a logic signal down the right
margin. Near-white drafting ground, one structural blue for live messages, one signal-orange for
the token trace, B612 / B612 Mono. The direction contract is an HTML comment at the top of
`index.html`'s `<body>` (seed `1ecf0e2c`). Durable visual decisions: `../DESIGN.md`.

## Commands

```bash
npm run dev        # Vite, port 5175
npm run build      # tsc --noEmit + vite build → dist/
npm run typecheck  # tsc --noEmit
npm run lint       # ESLint (flat config)
npm run bootstrap  # one-time provisioning against the live stack — see README
```

No test runner here — `packages/e2e` (Playwright, drives `packages/examples/react-vite`) is the
SDK's browser regression net. A demo Playwright spec was deliberately skipped (it would mint real
prod users).

## Architecture

- **`src/flow/protocol.ts`** — the static model: 3 actors, 5 steps, 11 ordered messages, each
  `observed` (the browser really made this call — solid, real wire event) or `narrated`
  (top-level redirect / server-to-server — drawn as the protocol, dashed, labelled, linked to
  the CI proof). Plus `TOKEN_EDGES` for the logic channel.
- **`src/wire/store.ts`** — `makeInstrumentedFetch(base)` wraps the fetch given to
  `<DusterProvider>`; for `/duster/api/v1/*` calls it clones the response, reads a safe header
  subset + the `/me` body, and records a `WireEvent`. Everything shown is genuine.
- **`src/model/flowState.ts`** — `useFlowState()` combines `useDuster().status` with the wire
  event stream into `{ phase, reached, rows, token, me, csrf }`. `phase`:
  `ready → authenticated → refreshed → ended`.
- **`src/trace/`** — `geometry.ts` (pure layout math, fixed 1000-unit viewBox) + `Trace.tsx`
  (the SVG: lifelines, time ruler with a draggable scrub handle, step bands, message arrows,
  token channel, an always-in-DOM `<TranscriptFallback>` that is the a11y / mobile reading).
- **`src/Controls.tsx`** — the entire integration surface: `useDuster()` + `login/logout/refresh`.
  Shown verbatim in the Code panel.
- **`src/panels/`** — Readout (session state word + controls), Narration (per-step prose),
  CodePanel (`?raw` import of the running source, active line banded), MePanel (`/me` body),
  WirePanel (the real request log).
- **`src/config.ts`** — resolves `clientId` from `window.__DEMO__` (prod, injected by
  `entrypoint.sh`) → `?client_id=` → `sessionStorage` → `VITE_DEMO_CLIENT_ID`.
- **`src/main.tsx`** — path router: `/error` → `ErrorPage` (reads `readDusterError`), else
  `<DusterProvider onUnauthenticated="ignore">`. `?replay` (dev only) swaps in a canned capture.

## Runtime config & deploy

`client_id` is minted at provisioning time (`scripts/bootstrap.ts`), never baked in.
`public/config.template.js` → `entrypoint.sh` `envsubst` → `/config/config.js`, loaded before
the bundle (`index.html`). `Dockerfile` serves the CI-built `dist/`; `demo.yaml` mirrors
`frontend.yaml`. `k8s/` = Kustomize base + `overlays/example`; the Ingress path-routes `/duster`
(no rewrite) to the in-cluster `duster` Service — tier-0 same-origin. Runs in the `authos` ns.

## Conventions

- No CSS framework — `src/styles.css` is one hand-authored stylesheet (the "Handshake Trace"
  world). No Tailwind.
- The direction contract HTML comment in `index.html` must survive the production build (grep
  `dist/index.html` for `1ecf0e2c`). Re-open it on every edit.
- Narration stays honest: never show a "token exchanged" UI for something the browser can't
  observe — label it narrated, link the proof.
