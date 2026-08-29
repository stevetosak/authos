import { describe, expect, it } from 'vitest'
import * as api from '../src/index.js'

describe('public entry', () => {
  it('exports the documented surface', () => {
    expect(Object.keys(api).sort()).toEqual(
      [
        'buildLogoutUrl',
        'buildMeUrl',
        'buildStartUrl',
        'buildUrl',
        'createDusterClient',
        'getOrCreateDusterClient',
        'normalizeUser',
        'readDusterError',
        'resetDusterRegistry',
      ].sort(),
    )
  })

  it('createDusterClient returns a client with the full interface', () => {
    const client = api.createDusterClient({ clientId: 'app_1', fetch: (() => Promise.resolve(new Response('{}'))) as unknown as typeof fetch })
    for (const method of ['getSnapshot', 'getServerSnapshot', 'subscribe', 'init', 'refresh', 'login', 'logout', 'destroy'] as const) {
      expect(typeof client[method]).toBe('function')
    }
  })
})
