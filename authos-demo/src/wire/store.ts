/**
 * A tiny observable store of the real HTTP the SDK put on the wire, plus a `fetch` wrapper that
 * feeds it. Everything here is genuine: the requests are the ones `@authoss/duster-react` made,
 * the status codes and headers are what Duster actually returned.
 */

export interface WireEvent {
  id: number
  /** ms since the first recorded event. */
  t: number
  method: string
  /** Path only, query stripped except `client_id`. */
  path: string
  status: number
  ok: boolean
  durationMs: number
  /** Safe subset — presence of the CSRF header, content-type, cache directives. */
  resHeaders: Record<string, string>
  /** For `/me`: the response body (every value a string, as Duster sends it). */
  body: Record<string, string> | null
  kind: 'me' | 'logout' | 'other'
}

type Listener = () => void

class WireStore {
  private events: WireEvent[] = []
  private seq = 0
  private origin = 0
  private listeners = new Set<Listener>()
  /** Set true once `login()` has been called in a previous page (sessionStorage breadcrumb). */
  redirectedAway = false

  // Arrow field: passed by reference to useSyncExternalStore, so it must stay bound.
  snapshot = (): readonly WireEvent[] => {
    return this.events
  }

  subscribe = (fn: Listener): (() => void) => {
    this.listeners.add(fn)
    return () => this.listeners.delete(fn)
  }

  private emit(): void {
    this.listeners.forEach((l) => l())
  }

  reset(): void {
    this.events = []
    this.seq = 0
    this.origin = 0
    this.emit()
  }

  record(e: Omit<WireEvent, 'id' | 't'>): void {
    const now = performance.now()
    if (this.events.length === 0) this.origin = now
    this.events = [
      ...this.events,
      { ...e, id: ++this.seq, t: Math.max(0, Math.round(now - this.origin)) },
    ]
    this.emit()
  }

  last(kind: WireEvent['kind']): WireEvent | undefined {
    for (let i = this.events.length - 1; i >= 0; i--) {
      if (this.events[i]!.kind === kind) return this.events[i]
    }
    return undefined
  }
}

export const wire = new WireStore()

const SAFE_RES_HEADERS = ['x-duster-csrf', 'content-type', 'cache-control', 'set-cookie']

function kindFor(path: string): WireEvent['kind'] {
  if (path.endsWith('/me')) return 'me'
  if (path.endsWith('/logout')) return 'logout'
  return 'other'
}

function tidyPath(rawUrl: string): string {
  try {
    const u = new URL(rawUrl, window.location.origin)
    const cid = u.searchParams.get('client_id')
    return cid ? `${u.pathname}?client_id=${cid.slice(0, 8)}…` : u.pathname
  } catch {
    return rawUrl
  }
}

/**
 * Builds the `fetch` for `<DusterProvider fetch={…}>`. Passes everything through the `base` fetch
 * untouched; for calls to the Duster mount it clones the response, reads the safe bits, and records
 * a WireEvent. `base` is the real `fetch` in production, the replay stub in `?replay` dev mode.
 */
export function makeInstrumentedFetch(base: typeof fetch = fetch): typeof fetch {
  return (input, init) => {
    const url =
      typeof input === 'string' ? input : input instanceof URL ? input.href : input.url
    if (!url.includes('/duster/api/v1/')) return base(input, init)

    const method = (init?.method ?? 'GET').toUpperCase()
    const started = performance.now()

    return base(input, init).then(async (res) => {
      const durationMs = Math.round(performance.now() - started)
      const path = tidyPath(url)
      const kind = kindFor(path.split('?')[0]!)

      const resHeaders: Record<string, string> = {}
      for (const h of SAFE_RES_HEADERS) {
        const v = res.headers.get(h)
        if (v !== null) resHeaders[h] = h === 'x-duster-csrf' ? redactCsrf(v) : v
      }
      if (!('x-duster-csrf' in resHeaders) && kind === 'me' && res.ok) {
        resHeaders['x-duster-csrf'] = '—'
      }

      let body: Record<string, string> | null = null
      if (kind === 'me' && res.ok) {
        try {
          body = (await res.clone().json()) as Record<string, string>
        } catch {
          body = null
        }
      }

      wire.record({
        method,
        path,
        status: res.status,
        ok: res.ok,
        durationMs,
        resHeaders,
        body,
        kind,
      })
      return res
    })
  }
}

function redactCsrf(v: string): string {
  return v.length > 12 ? `${v.slice(0, 8)}…${v.slice(-4)}` : v
}
