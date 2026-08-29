import { describe, expect, it } from 'vitest'
import { provideDuster } from '../src/provide-duster.js'
import { fakeFetch } from './helpers.js'

describe('provideDuster', () => {
  it('returns EnvironmentProviders without touching an injection context', () => {
    const providers = provideDuster({
      clientId: 'app_test',
      fetch: fakeFetch().fetch,
      onUnauthenticated: 'ignore',
    })
    expect(providers).toBeTypeOf('object')
    expect(providers).not.toBeNull()
  })
})
