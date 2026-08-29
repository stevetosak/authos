import { describe, expect, it, vi } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { useDuster } from '../src/useDuster.js'
import { DusterInjectionKey } from '../src/plugin.js'
import { dusterPlugin, fakeFetch, mountComposable } from './helpers.js'

describe('createDuster', () => {
  it('provides a client and runs exactly one /me', async () => {
    const { fetch, calls } = fakeFetch()
    const { result } = mountComposable(() => useDuster(), dusterPlugin(fetch))
    await flushPromises()
    expect(result.status.value).toBe('authenticated')
    expect(calls.filter((u) => u.includes('/me'))).toHaveLength(1)
  })

  it('shares one client (one /me) across two apps with the same clientId', async () => {
    const { fetch, calls } = fakeFetch()
    mountComposable(() => useDuster(), dusterPlugin(fetch))
    mountComposable(() => useDuster(), dusterPlugin(fetch))
    await flushPromises()
    expect(calls.filter((u) => u.includes('/me'))).toHaveLength(1)
  })

  it('threads config through — an onUnauthenticated function fires once on a 401', async () => {
    const onUnauthenticated = vi.fn()
    const { fetch } = fakeFetch({ authed: false })
    mountComposable(() => useDuster(), dusterPlugin(fetch, { onUnauthenticated }))
    await flushPromises()
    expect(onUnauthenticated).toHaveBeenCalledTimes(1)
    expect(onUnauthenticated.mock.lastCall?.[0]).toMatchObject({ reason: 'no-session' })
  })

  it('exports an injection key symbol', () => {
    expect(typeof DusterInjectionKey).toBe('symbol')
  })
})
