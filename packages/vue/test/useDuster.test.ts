import { describe, expect, it, vi } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { useDuster } from '../src/useDuster.js'
import { dusterPlugin, fakeFetch, mountComposable } from './helpers.js'

describe('useDuster', () => {
  it('throws when used without the plugin', () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => {})
    expect(() => mountComposable(() => useDuster())).toThrow(/requires the Duster plugin/)
    warn.mockRestore()
  })

  it('starts at loading then resolves to authenticated with the normalized user', async () => {
    const { fetch } = fakeFetch()
    const { result } = mountComposable(() => useDuster(), dusterPlugin(fetch))
    expect(result.status.value).toBe('loading')
    await flushPromises()
    expect(result.status.value).toBe('authenticated')
    expect(result.user.value?.email).toBe('jane@example.com')
    expect(result.user.value?.emailVerified).toBe(true)
    expect(result.error.value).toBeNull()
  })

  it('resolves to unauthenticated on a 401', async () => {
    const { fetch } = fakeFetch({ authed: false })
    const { result } = mountComposable(() => useDuster(), dusterPlugin(fetch))
    await flushPromises()
    expect(result.status.value).toBe('unauthenticated')
    expect(result.user.value).toBeNull()
  })

  it('login / logout / refresh call through to the client', async () => {
    const { fetch, calls } = fakeFetch()
    const { result } = mountComposable(() => useDuster(), dusterPlugin(fetch))
    await flushPromises()

    result.login()
    expect(vi.mocked(window.location.assign)).toHaveBeenCalledTimes(1)

    await result.logout({ redirectTo: '/x' })
    expect(calls.some((u) => u.includes('/logout'))).toBe(true)
  })

  it('reuses the user object across an identical refresh (no reactive churn)', async () => {
    const { fetch } = fakeFetch()
    const { result } = mountComposable(() => useDuster(), dusterPlugin(fetch))
    await flushPromises()
    const first = result.user.value
    await result.refresh()
    await flushPromises()
    // core reuses the object when /me is unchanged, so the computed never re-emits
    expect(result.user.value).toBe(first)
  })
})
