import { afterEach, describe, expect, it, vi } from 'vitest'
import { getOrCreateDusterClient, resetDusterRegistry } from '../src/registry.js'
import { fakeDuster } from './fake-duster.js'

afterEach(() => {
  resetDusterRegistry()
  vi.restoreAllMocks()
})

describe('getOrCreateDusterClient', () => {
  it('returns the same instance for the same clientId', () => {
    const { fetch } = fakeDuster()
    const a = getOrCreateDusterClient({ clientId: 'app_1', fetch })
    const b = getOrCreateDusterClient({ clientId: 'app_1', fetch })
    expect(a).toBe(b)
  })

  it('returns distinct instances for distinct clientIds', () => {
    const { fetch } = fakeDuster()
    const a = getOrCreateDusterClient({ clientId: 'app_1', fetch })
    const b = getOrCreateDusterClient({ clientId: 'app_2', fetch })
    expect(a).not.toBe(b)
  })

  it('keeps the first client and warns when a second call passes a different config', () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => {})
    const { fetch } = fakeDuster()
    const a = getOrCreateDusterClient({ clientId: 'app_1', fetch })
    const b = getOrCreateDusterClient({ clientId: 'app_1', fetch, baseUrl: 'https://other.test' })
    expect(b).toBe(a)
    expect(warn).toHaveBeenCalledOnce()
  })

  it('does not warn when the second config is equivalent', () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => {})
    const { fetch } = fakeDuster()
    getOrCreateDusterClient({ clientId: 'app_1', fetch, baseUrl: 'https://a.test' })
    getOrCreateDusterClient({ clientId: 'app_1', fetch, baseUrl: 'https://a.test' })
    expect(warn).not.toHaveBeenCalled()
  })

  it('resetDusterRegistry forgets cached clients', () => {
    const { fetch } = fakeDuster()
    const a = getOrCreateDusterClient({ clientId: 'app_1', fetch })
    resetDusterRegistry()
    const b = getOrCreateDusterClient({ clientId: 'app_1', fetch })
    expect(a).not.toBe(b)
  })
})
