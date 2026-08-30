/**
 * Runtime config resolution, in priority order:
 *  1. `window.__DEMO__`      — injected by entrypoint.sh at container start (production)
 *  2. `?client_id=…`         — first navigation carries it; stashed for the login round-trip
 *  3. `sessionStorage`       — the stash, so it survives the trip back to `success_url` (`/`)
 *  4. `import.meta.env.VITE_DEMO_CLIENT_ID` — `npm run dev` against a local seeded stack
 */

interface DemoWindow {
  clientId?: string
  dusterBasePath?: string
  authosOrigin?: string
  proofSpecUrl?: string
  proofCiUrl?: string
}

const injected: DemoWindow =
  (window as unknown as { __DEMO__?: DemoWindow }).__DEMO__ ?? {}

const STASH_KEY = 'authos-demo:client_id'

function readStash(): string | null {
  try {
    return window.sessionStorage.getItem(STASH_KEY)
  } catch {
    return null
  }
}

function writeStash(value: string): void {
  try {
    window.sessionStorage.setItem(STASH_KEY, value)
  } catch {
    /* private mode — the query value carries this session */
  }
}

const query = new URLSearchParams(window.location.search)
const fromQuery = query.get('client_id')
if (fromQuery) writeStash(fromQuery)

// `${DEMO_…}` means envsubst had no value — treat as unset.
const clean = (v: string | undefined): string | undefined =>
  v && !v.startsWith('${') ? v : undefined

const clientId =
  clean(injected.clientId) ??
  fromQuery ??
  readStash() ??
  (import.meta.env.VITE_DEMO_CLIENT_ID as string | undefined) ??
  ''

export const config = {
  clientId,
  dusterBasePath: clean(injected.dusterBasePath) ?? '/duster/api/v1',
  /** The public Authos origin — for display only; the SDK builds its own URLs. */
  authosOrigin: clean(injected.authosOrigin) ?? 'https://authos-api.tosak.net',
  proof: {
    spec:
      clean(injected.proofSpecUrl) ??
      'https://github.com/stevetosak/authos/blob/master/packages/e2e/specs/login-refresh-logout.spec.ts',
    ci:
      clean(injected.proofCiUrl) ??
      'https://github.com/stevetosak/authos/actions/workflows/sdk.yaml',
  },
  /** Dev-only: `?replay=1` feeds a canned capture so the trace renders with no live backend. */
  replay: import.meta.env.DEV && query.has('replay'),
  hasClientId: clientId.length > 0,
}

export type Config = typeof config
