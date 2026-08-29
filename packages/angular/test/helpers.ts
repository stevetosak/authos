import type { DusterConfig } from '@authoss/duster-core'
import { DusterService } from '../src/duster.service.js'

export interface FakeFetch {
  fetch: typeof fetch
  calls: string[]
  setAuthed: (v: boolean) => void
}

/** A `fetch` stub encoding the slice of the Duster wire contract the adapter cares about. */
export function fakeFetch(
  opts: { authed?: boolean; body?: Record<string, string>; csrf?: string; meStatus?: number } = {},
): FakeFetch {
  let authed = opts.authed ?? true
  const body = opts.body ?? {
    sub: 'ppid-jane',
    name: 'Jane Doe',
    email: 'jane@example.com',
    emailVerified: 'true',
    phoneNumberVerified: 'false',
  }
  const csrf = opts.csrf ?? 'csrf-1'
  const calls: string[] = []

  const fn = async (input: string | URL | Request): Promise<Response> => {
    const url = typeof input === 'string' ? input : input.toString()
    calls.push(url)
    if (url.includes('/me')) {
      if (opts.meStatus && opts.meStatus >= 500) {
        return new Response(`${opts.meStatus}: boom`, {
          status: opts.meStatus,
          headers: { 'content-type': 'text/plain' },
        })
      }
      if (!authed) return new Response(null, { status: 401 })
      return new Response(JSON.stringify(body), {
        status: 200,
        headers: { 'content-type': 'application/json', 'x-duster-csrf': csrf },
      })
    }
    if (url.includes('/logout')) {
      authed = false
      return new Response(null, { status: 302, headers: { location: '/' } })
    }
    return new Response('not found', { status: 404 })
  }

  return {
    fetch: fn as unknown as typeof fetch,
    calls,
    setAuthed: (v) => {
      authed = v
    },
  }
}

/** A `DusterService` wired to a stub fetch, `onUnauthenticated: 'ignore'` so a 401 doesn't navigate. */
export function makeService(fetchImpl: typeof fetch, extra: Partial<DusterConfig> = {}): DusterService {
  return new DusterService({
    clientId: 'app_test',
    fetch: fetchImpl,
    onUnauthenticated: 'ignore',
    ...extra,
  })
}
