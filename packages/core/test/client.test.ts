import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createDusterClient } from '../src/client.js'
import type { DusterConfig } from '../src/types.js'
import { fakeDuster, type FakeDusterState } from './fake-duster.js'
import { installDom } from './env.js'

function setup(
  state: Partial<FakeDusterState> = {},
  config: Partial<DusterConfig> = {},
  domPath = '/dashboard',
) {
  const dom = installDom({ path: domPath })
  const duster = fakeDuster(state)
  const client = createDusterClient({ clientId: 'app_123', fetch: duster.fetch, ...config })
  return { client, ...duster, ...dom }
}

afterEach(() => {
  vi.unstubAllGlobals()
  vi.restoreAllMocks()
})

describe('init / status machine', () => {
  it('resolves to authenticated with a normalized user on 200', async () => {
    const { client } = setup()
    await client.init()
    const snap = client.getSnapshot()
    expect(snap.status).toBe('authenticated')
    expect(snap.user?.email).toBe('jane@example.com')
    expect(snap.user?.emailVerified).toBe(true)
    expect(snap.error).toBeNull()
  })

  it('starts at loading before init resolves', () => {
    const { client } = setup()
    expect(client.getSnapshot().status).toBe('loading')
  })

  it('notifies subscribers on a real change and returns a stable snapshot otherwise', async () => {
    const { client } = setup()
    const listener = vi.fn()
    client.subscribe(listener)
    await client.init()
    expect(listener).toHaveBeenCalledTimes(1)

    const first = client.getSnapshot()
    await client.refresh() // identical /me response → no state change
    expect(client.getSnapshot()).toBe(first)
    expect(listener).toHaveBeenCalledTimes(1)
  })

  it('unsubscribe stops further notifications', async () => {
    const { client } = setup()
    const listener = vi.fn()
    const off = client.subscribe(listener)
    off()
    await client.init()
    expect(listener).not.toHaveBeenCalled()
  })

  it('is idempotent — two init() calls make one /me request', async () => {
    const { client, state } = setup()
    await Promise.all([client.init(), client.init()])
    expect(state.calls.filter((c) => c.url.includes('/me'))).toHaveLength(1)
  })
})

describe('unauthenticated handling', () => {
  it('goes unauthenticated on a 401 and fires onUnauthenticated once with reason no-session', async () => {
    const onUnauthenticated = vi.fn()
    const { client } = setup({ session: false }, { onUnauthenticated })
    await client.init()
    expect(client.getSnapshot().status).toBe('unauthenticated')
    expect(client.getSnapshot().user).toBeNull()
    expect(onUnauthenticated).toHaveBeenCalledTimes(1)
    expect(onUnauthenticated.mock.lastCall![0]).toMatchObject({ reason: 'no-session' })
  })

  it('uses reason revalidation-failed when a live session later 401s', async () => {
    const onUnauthenticated = vi.fn()
    const { client, state } = setup({}, { onUnauthenticated })
    await client.init()
    state.session = false
    await client.refresh()
    expect(onUnauthenticated).toHaveBeenCalledTimes(1)
    expect(onUnauthenticated.mock.lastCall![0]).toMatchObject({ reason: 'revalidation-failed' })
  })

  it("onUnauthenticated: 'ignore' does not navigate", async () => {
    const { client, window } = setup({ session: false }, { onUnauthenticated: 'ignore' })
    await client.init()
    expect(window.location.assign).not.toHaveBeenCalled()
  })

  it("onUnauthenticated: 'redirect' (default) navigates to /oauth/start", async () => {
    const { client, window } = setup({ session: false })
    await client.init()
    expect(window.location.assign).toHaveBeenCalledWith('/duster/api/v1/oauth/start?client_id=app_123')
  })

  it('passes a working login() and returnTo to a function handler', async () => {
    const onUnauthenticated = vi.fn()
    const { client, window } = setup({ session: false }, { onUnauthenticated }, '/reports?tab=q3')
    await client.init()
    const ctx = onUnauthenticated.mock.lastCall![0]
    expect(ctx.returnTo).toBe('/reports?tab=q3')
    ctx.login()
    expect(window.location.assign).toHaveBeenCalledOnce()
    expect(window.sessionStorage.getItem('duster:return-to')).toBe('/reports?tab=q3')
  })
})

describe('server / network errors are not auth failures', () => {
  it('a 500 on initial load keeps status loading, sets error, never fires onUnauthenticated', async () => {
    vi.useFakeTimers()
    const onUnauthenticated = vi.fn()
    const { client } = setup({ meStatus: 500 }, { onUnauthenticated })
    const done = client.init()
    await vi.runAllTimersAsync()
    await done
    const snap = client.getSnapshot()
    expect(snap.status).toBe('loading')
    expect(snap.error).toMatchObject({ kind: 'server', status: 500 })
    expect(onUnauthenticated).not.toHaveBeenCalled()
    vi.useRealTimers()
  })

  it('a network error on initial load sets a network error, keeps loading', async () => {
    vi.useFakeTimers()
    const { client } = setup({ meNetworkError: true })
    const done = client.init()
    await vi.runAllTimersAsync()
    await done
    expect(client.getSnapshot().status).toBe('loading')
    expect(client.getSnapshot().error?.kind).toBe('network')
    vi.useRealTimers()
  })

  it('retries during init and recovers when /me starts succeeding', async () => {
    vi.useFakeTimers()
    const { client, state } = setup({ meStatus: 503 })
    const done = client.init()
    // let the first attempt fail, then fix the server and advance through the backoff
    await vi.advanceTimersByTimeAsync(0)
    state.meStatus = null
    await vi.advanceTimersByTimeAsync(5000)
    await done
    expect(client.getSnapshot().status).toBe('authenticated')
    vi.useRealTimers()
  })

  it('refresh() (not init) makes a single attempt and leaves a prior authenticated state intact', async () => {
    const { client, state } = setup()
    await client.init()
    state.meStatus = 500
    await client.refresh()
    const snap = client.getSnapshot()
    expect(snap.status).toBe('authenticated')
    expect(snap.user?.email).toBe('jane@example.com')
    expect(snap.error).toMatchObject({ kind: 'server', status: 500 })
  })
})

describe('logout', () => {
  it('POSTs /logout, clears local state, navigates to postLogoutRedirect', async () => {
    const { client, state, window } = setup({}, { postLogoutRedirect: '/bye' })
    await client.init()
    await client.logout()
    const logoutCall = state.calls.find((c) => c.url.includes('/logout'))
    expect(logoutCall?.method).toBe('POST')
    expect(client.getSnapshot().status).toBe('unauthenticated')
    expect(client.getSnapshot().user).toBeNull()
    expect(window.location.assign).toHaveBeenLastCalledWith('/bye')
  })

  it('replays the X-Duster-Csrf token captured from /me (tier 1)', async () => {
    const { client, state } = setup({ tier1: true })
    await client.init()
    await client.logout()
    const logoutCall = state.calls.find((c) => c.url.includes('/logout'))
    expect(logoutCall?.headers['x-duster-csrf']).toBe('csrf-token-abc')
    expect(state.session).toBe(false)
  })

  it('fetches a CSRF token first when logout() is called before any /me (tier 1)', async () => {
    const { client, state } = setup({ tier1: true })
    await client.logout()
    const meCalls = state.calls.filter((c) => c.url.includes('/me'))
    expect(meCalls.length).toBeGreaterThanOrEqual(1)
    expect(state.session).toBe(false)
  })

  it('skips the POST when the pre-logout /me probe shows no session', async () => {
    const { client, state, window } = setup({ session: false })
    await client.logout()
    expect(state.calls.some((c) => c.url.includes('/logout'))).toBe(false)
    expect(window.location.assign).toHaveBeenCalledOnce()
  })

  it('clears local state and navigates even if every logout request throws', async () => {
    installDom({ path: '/x' })
    const client = createDusterClient({
      clientId: 'app_123',
      fetch: (() => Promise.reject(new TypeError('offline'))) as unknown as typeof fetch,
      postLogoutRedirect: '/bye',
    })
    await client.logout()
    expect(client.getSnapshot().status).toBe('unauthenticated')
    expect((window as unknown as { location: { assign: ReturnType<typeof vi.fn> } }).location.assign)
      .toHaveBeenCalledWith('/bye')
  })
})

describe('login', () => {
  it('is synchronous, stashes returnTo, and assigns to /oauth/start', () => {
    const { client, window } = setup({}, {}, '/settings')
    const result = client.login()
    expect(result).toBeUndefined()
    expect(window.sessionStorage.getItem('duster:return-to')).toBe('/settings')
    expect(window.location.assign).toHaveBeenCalledWith('/duster/api/v1/oauth/start?client_id=app_123')
  })

  it('honors an explicit returnTo', () => {
    const { client, window } = setup()
    client.login({ returnTo: '/deep/link' })
    expect(window.sessionStorage.getItem('duster:return-to')).toBe('/deep/link')
  })
})

describe('revalidation', () => {
  it('re-checks /me when the tab becomes visible and revalidateOnFocus is on', async () => {
    const { client, state, document } = setup({}, { revalidateOnFocus: true })
    await client.init()
    const before = state.calls.filter((c) => c.url.includes('/me')).length
    document.emit('visibilitychange')
    await vi.waitFor(() =>
      expect(state.calls.filter((c) => c.url.includes('/me')).length).toBe(before + 1),
    )
  })

  it('does not attach a visibility listener when revalidateOnFocus is off', async () => {
    const { client, state, document } = setup()
    await client.init()
    const before = state.calls.length
    document.emit('visibilitychange')
    expect(state.calls.length).toBe(before)
  })

  it('destroy() detaches listeners and clears subscribers', async () => {
    const { client, state, document } = setup({}, { revalidateOnFocus: true })
    await client.init()
    client.destroy()
    const before = state.calls.length
    document.emit('visibilitychange')
    expect(state.calls.length).toBe(before)
  })
})

describe('SSR / no window', () => {
  beforeEach(() => {
    vi.unstubAllGlobals() // ensure window/document are truly undefined
  })

  it('getServerSnapshot is a constant loading snapshot', () => {
    const client = createDusterClient({ clientId: 'app_123', fetch: fakeDuster().fetch })
    expect(client.getServerSnapshot()).toEqual({ user: null, status: 'loading', error: null })
  })

  it('login() warns and does not throw without a window', () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => {})
    const client = createDusterClient({ clientId: 'app_123', fetch: fakeDuster().fetch })
    expect(() => client.login()).not.toThrow()
    expect(warn).toHaveBeenCalled()
  })

  it('init() still fetches /me and can resolve authenticated without a window', async () => {
    const duster = fakeDuster()
    const client = createDusterClient({ clientId: 'app_123', fetch: duster.fetch })
    await client.init()
    expect(client.getSnapshot().status).toBe('authenticated')
  })
})

describe('config guard', () => {
  it('an empty clientId yields a config error and a no-op init', async () => {
    const duster = fakeDuster()
    const client = createDusterClient({ clientId: '', fetch: duster.fetch })
    const snap = client.getSnapshot()
    expect(snap.status).toBe('unauthenticated')
    expect(snap.error?.kind).toBe('config')
    await client.init()
    expect(duster.state.calls).toHaveLength(0)
  })
})
