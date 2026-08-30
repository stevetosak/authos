/**
 * DEV-ONLY. `?replay` swaps the network for a canned capture so the trace renders with no live
 * backend — for local design work and screenshots. Stripped from production builds by the
 * `import.meta.env.DEV` gate in config.ts; the UI shows a "REPLAY" banner whenever it is active.
 */

export const replayClientId = 'replay-client-0000'

const query = new URLSearchParams(window.location.search)
const mode = query.get('replay') || 'auth' // '' → 'auth'
export const replayMode: 'ready' | 'auth' = mode === 'ready' ? 'ready' : 'auth'

const ME_BODY = {
  sub: '7f3c1a9e4b2d6f80a1c5e7d9b3f5a2c48e0d6b1f9a3c7e5d2b8f4a6c0e1d3b5f7',
  email: 'ada@example.com',
  email_verified: 'true',
  name: 'Ada Lovelace',
  given_name: 'Ada',
  family_name: 'Lovelace',
  updated_at: '1735689600',
}

export const replayFetch: typeof fetch = async (input, init) => {
  const url = typeof input === 'string' ? input : input instanceof URL ? input.href : input.url
  const method = (init?.method ?? 'GET').toUpperCase()

  await new Promise((r) => setTimeout(r, 120))

  if (url.includes('/me')) {
    if (replayMode === 'ready') {
      return new Response('', { status: 401 })
    }
    return new Response(JSON.stringify(ME_BODY), {
      status: 200,
      headers: {
        'content-type': 'application/json',
        'x-duster-csrf': '9b1c7f42-a0d3-4e51-8f6a-2c9e7d5b3a1f',
      },
    })
  }
  if (url.includes('/logout') && method === 'POST') {
    return new Response('', { status: 302, headers: { location: '/' } })
  }
  return new Response('', { status: 404 })
}
