import { describe, expect, it, vi } from 'vitest'
import { fakeFetch, makeService } from './helpers.js'

describe('DusterService', () => {
  it('starts at loading, then authenticated with the normalized user', async () => {
    const svc = makeService(fakeFetch().fetch)
    expect(svc.status()).toBe('loading')
    await svc.ensureInitialized()
    expect(svc.status()).toBe('authenticated')
    expect(svc.user()?.email).toBe('jane@example.com')
    expect(svc.user()?.emailVerified).toBe(true)
    expect(svc.error()).toBeNull()
  })

  it('resolves to unauthenticated on a 401', async () => {
    const svc = makeService(fakeFetch({ authed: false }).fetch)
    await svc.ensureInitialized()
    expect(svc.status()).toBe('unauthenticated')
    expect(svc.user()).toBeNull()
  })

  it('ensureInitialized dedupes concurrent and repeat calls into one /me', async () => {
    const { fetch, calls } = fakeFetch()
    const svc = makeService(fetch)
    await Promise.all([svc.ensureInitialized(), svc.ensureInitialized()])
    await svc.ensureInitialized()
    expect(calls.filter((u) => u.includes('/me'))).toHaveLength(1)
  })

  it('login / logout / refresh delegate to the core client', async () => {
    const { fetch, calls } = fakeFetch()
    const svc = makeService(fetch)
    await svc.ensureInitialized()

    svc.login()
    expect(vi.mocked(window.location.assign)).toHaveBeenCalledTimes(1)

    await svc.logout({ redirectTo: '/x' })
    expect(calls.some((u) => u.includes('/logout'))).toBe(true)
  })

  it('session$ emits the current snapshot on subscribe, then every change', async () => {
    const svc = makeService(fakeFetch().fetch)
    const seen: string[] = []
    const sub = svc.session$.subscribe((s) => seen.push(s.status))
    await svc.ensureInitialized()
    sub.unsubscribe()
    expect(seen[0]).toBe('loading')
    expect(seen.at(-1)).toBe('authenticated')
  })

  it('ngOnDestroy detaches the store subscription', async () => {
    const ff = fakeFetch()
    const svc = makeService(ff.fetch)
    await svc.ensureInitialized()
    expect(svc.status()).toBe('authenticated')

    svc.ngOnDestroy()
    ff.setAuthed(false)
    await svc.refresh()
    // subscription gone → the signal stays put even though /me now 401s
    expect(svc.status()).toBe('authenticated')
  })
})
