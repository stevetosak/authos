import { afterEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { h, nextTick } from 'vue'
import type { Slots } from 'vue'
import { Protected, ProtectedRoute } from '../src/index.js'
import { dusterPlugin, fakeFetch } from './helpers.js'

afterEach(() => vi.useRealTimers())

function mountGate(
  slots: Record<string, () => unknown>,
  fetchOpts: Parameters<typeof fakeFetch>[0] = {},
) {
  const { fetch } = fakeFetch(fetchOpts)
  return mount(ProtectedRoute, {
    global: { plugins: [dusterPlugin(fetch)] },
    slots: slots as unknown as Partial<Slots>,
  })
}

describe('<ProtectedRoute>', () => {
  it('renders the loading slot, then the default slot once authenticated', async () => {
    const w = mountGate({ loading: () => h('p', 'checking'), default: () => h('p', 'secret') })
    expect(w.text()).toContain('checking')
    await flushPromises()
    expect(w.text()).toContain('secret')
  })

  it('renders the fallback slot when unauthenticated', async () => {
    const w = mountGate(
      { fallback: () => h('p', 'please log in'), default: () => h('p', 'secret') },
      { authed: false },
    )
    await flushPromises()
    expect(w.text()).toContain('please log in')
    expect(w.text()).not.toContain('secret')
  })

  it('renders the errorFallback slot on a server error while not authenticated', async () => {
    vi.useFakeTimers()
    const { fetch } = fakeFetch({ meStatus: 503 })
    const w = mount(ProtectedRoute, {
      global: { plugins: [dusterPlugin(fetch)] },
      slots: {
        loading: () => h('p', 'checking'),
        errorFallback: () => h('p', 'duster is down'),
        default: () => h('p', 'secret'),
      } as unknown as Partial<Slots>,
    })
    await vi.runAllTimersAsync() // drain the core's retry backoff
    await nextTick()
    expect(w.text()).toContain('duster is down')
    expect(w.text()).not.toContain('secret')
  })

  it('Protected is an alias of ProtectedRoute', () => {
    expect(Protected).toBe(ProtectedRoute)
  })
})
