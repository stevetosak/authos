import { describe, expect, it } from 'vitest'
import * as api from '../src/index.js'

describe('public entry', () => {
  it('exports the React surface plus re-exported core helpers/types', () => {
    expect(Object.keys(api).sort()).toEqual(
      [
        'DusterProvider',
        'Protected',
        'ProtectedRoute',
        'buildLogoutUrl',
        'buildMeUrl',
        'buildStartUrl',
        'normalizeUser',
        'readDusterError',
        'useDuster',
      ].sort(),
    )
  })
})
