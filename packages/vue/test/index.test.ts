import { describe, expect, it } from 'vitest'
import * as api from '../src/index.js'

describe('public entry', () => {
  it('exports the Vue surface plus re-exported core helpers', () => {
    expect(Object.keys(api).sort()).toEqual(
      [
        'DusterInjectionKey',
        'Protected',
        'ProtectedRoute',
        'buildLogoutUrl',
        'buildMeUrl',
        'buildStartUrl',
        'createDuster',
        'normalizeUser',
        'readDusterError',
        'useDuster',
      ].sort(),
    )
  })
})
