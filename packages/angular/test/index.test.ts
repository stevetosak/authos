import { describe, expect, it } from 'vitest'
import * as api from '../src/index.js'

describe('public entry', () => {
  it('exports the Angular surface plus re-exported core helpers', () => {
    expect(Object.keys(api).sort()).toEqual(
      [
        'DusterService',
        'buildLogoutUrl',
        'buildMeUrl',
        'buildStartUrl',
        'dusterAuthGuard',
        'normalizeUser',
        'provideDuster',
        'readDusterError',
        'resolveDusterAccess',
      ].sort(),
    )
  })
})
